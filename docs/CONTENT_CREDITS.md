# Content and audio credits

## English curriculum

Vocabulary selection follows the user's Python application (1,080 terms), combined
with the 60-word Java reference. After term deduplication and excluding the proper
name `erika`, 1,128 entries remain. Existing Java IDs and meanings are preserved.

English dictionary glosses come from
[ECDICT revision ab22ad36302cafc6349a050e2b60894855208055](https://github.com/skywind3000/ECDICT/tree/ab22ad36302cafc6349a050e2b60894855208055).

- [Pinned source CSV](https://raw.githubusercontent.com/skywind3000/ECDICT/ab22ad36302cafc6349a050e2b60894855208055/ecdict.csv)
- CSV SHA-256: `d1ecbdf9ee2b920afb6f430ca6b6bb9f559588cf0dff86e04bd255f9390843cc`
- [ECDICT MIT license](../src/main/resources/licenses/ECDICT-LICENSE.txt), Copyright (c) 2025 Linwei.
- Some upstream English definitions derive from WordNet; its notice is included as
  [WordNet license](../src/main/resources/licenses/WORDNET-LICENSE.txt).
- `tools/english-word-selection.json` retains the original term/level selection.
- `tools/english-gloss-overrides.json` contains project-authored English glosses.
- Each runtime row identifies its source. The generator takes one English dictionary
  sense, rather than claiming complete sense equivalence to the Chinese original.

## Recordings

The [Dictionary API](https://dictionaryapi.dev/) returns audio URLs and attribution.
Recordings are fetched only after a user chooses Listen. The UI retains provider
source/license details and the actual available accent. Commons recovery reads
the stated source page's metadata. A recording's license is separate from the API
and from the application's code license. Runtime recordings are not bundled.

The unmodified test fixture has its own
[creator, source and CC BY 3.0 US attribution](../src/test/resources/audio/ATTRIBUTION.txt).
It is used for silent decoder verification and is not auto-played.

## Audio libraries

- JLayer 1.0.1 (javazoom): LGPL. [Source artifact](https://repo.maven.apache.org/maven2/javazoom/jlayer/1.0.1/jlayer-1.0.1-sources.jar).
- VorbisSPI 1.0.3.3, JOrbis and Tritonus Share: LGPL.
  [VorbisSPI source artifact](https://repo.maven.apache.org/maven2/com/googlecode/soundlibs/vorbisspi/1.0.3.3/vorbisspi-1.0.3.3-sources.jar).
- Jackson Databind/Core/Annotations 2.17.2: Apache 2.0.
  [Jackson source and license](https://github.com/FasterXML/jackson-databind/tree/jackson-databind-2.17.2).

The dependency implementations are unmodified. The Maven build and source are
available so users can replace/relink the audio libraries. Packaged service
descriptors are merged to retain the Ogg Java Sound decoder.

Full LGPL 2.1 and Apache 2.0 license texts are bundled under src/main/resources/licenses.
