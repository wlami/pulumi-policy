#!/usr/bin/env bash
# Re-vendor proto files from the pulumi/pulumi submodule. Run when the
# upstream analyzer.proto / plugin.proto changes.
set -euo pipefail
script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
sdk_dir="$(cd "$script_dir/.." && pwd)"
src="$(cd "$sdk_dir/../../../pulumi/proto/pulumi" && pwd)"
cp "$src/analyzer.proto" "$sdk_dir/src/main/proto/pulumi/analyzer.proto"
cp "$src/plugin.proto"   "$sdk_dir/src/main/proto/pulumi/plugin.proto"
echo "synced analyzer.proto and plugin.proto from $src"
