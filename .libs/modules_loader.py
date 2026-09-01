import os

from modules import register_packages

# load current folder as python package
AGENTS_PACKAGE_PATH = os.environ.get('AGENTS_PACKAGE_PATH')

# load project workspace as python package
PROJECT_WORKSPACE_PACKAGE_PATH = os.environ.get('PROJECT_WORKSPACE')

# load GitHub workflow scripts folder as python package
GITHUB_WORKFLOWS_PACKAGE_PATH = ".github"
GITHUB_WORKFLOWS_SCRIPTS_PACKAGE_PATH = f"{GITHUB_WORKFLOWS_PACKAGE_PATH}/scripts"

# register packages
def load_modules():
    register_packages([
        AGENTS_PACKAGE_PATH,
        PROJECT_WORKSPACE_PACKAGE_PATH,
        GITHUB_WORKFLOWS_PACKAGE_PATH,
        GITHUB_WORKFLOWS_SCRIPTS_PACKAGE_PATH
    ])
# invoke
load_modules()