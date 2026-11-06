import importlib.util
import json
import os
from pathlib import Path
import tempfile
import unittest

spec = importlib.util.spec_from_file_location('ops', Path(__file__).parents[2] / 'scripts' / 'ops.py')
ops = importlib.util.module_from_spec(spec)
spec.loader.exec_module(ops)

class ManifestTests(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.file = Path(self.directory.name) / 'manifest.json'
        self.data = {'version': 1, 'dirty': False, 'image': 'ghcr.io/ermandotimoteo/precisei',
                     'digest': 'sha256:' + 'a' * 64, 'commit': 'b' * 40, 'jarSha256': 'c' * 64,
                     'schema': {'lineage': 'production', 'minimum': 3, 'maximum': 3}}
    def tearDown(self):
        self.directory.cleanup()
    def read(self):
        self.file.write_text(json.dumps(self.data), encoding='utf-8')
        return ops.manifest(self.file)
    def test_accepts_clean_candidate(self):
        self.assertEqual(self.read()['schema']['minimum'], 3)
    def test_rejects_another_registry(self):
        self.data['image'] = 'example.invalid/other/image'
        with self.assertRaises(ValueError): self.read()
    def test_rejects_dirty_candidate(self):
        self.data['dirty'] = True
        with self.assertRaises(ValueError): self.read()
    def test_rejects_digest_injection(self):
        self.data['digest'] = 'sha256:a; arbitrary-command'
        with self.assertRaises(ValueError): self.read()
    def test_rejects_invalid_range(self):
        self.data['schema']['maximum'] = 1
        with self.assertRaises(ValueError): self.read()
    def test_rejects_boolean_as_version(self):
        self.data['schema']['minimum'] = True
        with self.assertRaises(ValueError): self.read()
    def test_requires_jar_checksum(self):
        del self.data['jarSha256']
        with self.assertRaises(ValueError): self.read()
    def test_promotion_requires_same_digest(self):
        staged = dict(self.data, digest='sha256:'+'d'*64)
        with self.assertRaises(ValueError): ops.require_same(self.data, staged)
    def test_promotion_requires_same_source(self):
        staged = dict(self.data, commit='d'*40)
        with self.assertRaises(ValueError): ops.require_same(self.data, staged)
    def test_promotion_preserves_candidate(self):
        ops.require_same(self.data, dict(self.data))

if __name__ == '__main__': unittest.main()
