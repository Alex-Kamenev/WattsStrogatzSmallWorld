"""Build the historical simulation with an installed JDK; no downloads needed."""
from pathlib import Path
import argparse
import os
import shutil
import subprocess
import tempfile

root = Path(__file__).resolve().parent
parser = argparse.ArgumentParser()
parser.add_argument('--test', action='store_true', help='Run headless regressions before packaging')
args = parser.parse_args()

def tool(name):
    suffix = '.exe' if os.name == 'nt' else ''
    java_home = os.environ.get('JAVA_HOME')
    candidate = Path(java_home) / 'bin' / (name + suffix) if java_home else None
    found = str(candidate) if candidate and candidate.is_file() else shutil.which(name)
    if not found:
        raise SystemExit(f'{name} not found. Install JDK 17+ and set JAVA_HOME or PATH.')
    return found

target = root / 'target'
target.mkdir(exist_ok=True)
if not target.resolve().is_relative_to(root):
    raise SystemExit('Build target must remain inside this repository')
with tempfile.TemporaryDirectory(prefix='build-', dir=target) as temporary:
    stage = Path(temporary)
    classes = stage / 'classes'
    classes.mkdir()
    sources = sorted((root / 'src' / 'main' / 'java').rglob('*.java'))
    subprocess.run([tool('javac'), '--release', '17', '-encoding', 'UTF-8', '-d', str(classes), *map(str, sources)], check=True)
    if args.test:
        tests = stage / 'tests'
        tests.mkdir()
        test_sources = sorted((root / 'src' / 'test' / 'java').rglob('*.java'))
        subprocess.run([tool('javac'), '--release', '17', '-encoding', 'UTF-8', '-cp', str(classes), '-d', str(tests), *map(str, test_sources)], check=True)
        subprocess.run([tool('java'), '-Djava.awt.headless=true', '-cp', os.pathsep.join(map(str, [classes, tests])), 'com.lamaryw.adv.SimulationRegression'], check=True, timeout=30)
    archive = stage / 'SmallWorld.jar'
    subprocess.run([tool('jar'), '--create', '--file', str(archive), '--main-class', 'com.lamaryw.adv.WattsStrogatzSmallWorld', '-C', str(classes), '.'], check=True)
    archive.replace(target / 'SmallWorld.jar')
print('Built target/SmallWorld.jar. Run: java -jar target/SmallWorld.jar')
