import runpy
import sys

from modules_loader import load_modules

# load modules
load_modules()

# Check whether need to launch module
if len(sys.argv) > 1:
    # parse module name to run
    target_module = sys.argv[1]
    # cut 'launcher.py' name and keep all arguments of module to run
    sys.argv = sys.argv[1:]
    # run module with arguments from command-line (excluded launcher) in same process
    print(f"⚙🚀 Launching module {target_module} with arguments: { sys.argv }...")
    runpy.run_module(target_module, run_name='__main__')
