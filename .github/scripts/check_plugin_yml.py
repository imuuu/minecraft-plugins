# Fails when a plugin.yml is not valid YAML or repeats a key. Paper rejects the first
# at startup; the second silently keeps only the last value.
import glob
import sys

import yaml


class StrictLoader(yaml.SafeLoader):
    pass


def construct_mapping(loader, node, deep=False):
    keys = set()
    for key_node, _ in node.value:
        key = loader.construct_object(key_node, deep=deep)
        if key in keys:
            raise yaml.constructor.ConstructorError(
                None, None, f"duplicate key {key!r}", key_node.start_mark)
        keys.add(key)
    return loader.construct_mapping(node, deep)


StrictLoader.add_constructor(yaml.resolver.BaseResolver.DEFAULT_MAPPING_TAG, construct_mapping)

failed = False
for path in sorted(glob.glob("libs/*/src/main/resources/plugin.yml")
                   + glob.glob("plugins/*/src/main/resources/plugin.yml")):
    try:
        with open(path, encoding="utf-8") as f:
            yaml.load(f, Loader=StrictLoader)
        print(f"ok      {path}")
    except yaml.YAMLError as e:
        failed = True
        print(f"INVALID {path}\n{e}\n")
sys.exit(1 if failed else 0)
