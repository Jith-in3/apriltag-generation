# AprilTag-Generation: Custom Tag Family Guide

This document explains how `apriltag-generation` works and gives a verified,
step-by-step recipe for producing a **`custom_<string>`** tag family — a
flexible layout you design yourself, pixel by pixel — and turning it into the
`.c`/`.h` files consumed by the [`apriltag`](https://github.com/AprilRobotics/apriltag)
C detector.

Every command below was actually run against this checkout while writing this
guide (build, generation, `TagToC`, `GenerateTags`); the outputs shown are
real, not illustrative.

## 1. What this repo is

A small Java/Ant project with three independent CLI tools, run in sequence:

```
TagFamilyGenerator   layout description -> searches for a valid codebook -> prints Java source
TagToC                Java TagFamily class -> emits <name>.c / <name>.h for the C detector
GenerateTags          Java TagFamily class -> renders .png / .ps images for printing
```

It contains **no C code and no detection logic** — it only designs layouts
and codebooks. The C `apriltag` repo only *consumes* the `.c`/`.h` pair; it
cannot generate new families itself.

## 2. Toolchain / build

Requires a JDK (not just a JRE) and Ant:

```bash
sudo apt-get install -y openjdk-11-jdk-headless ant
```

Build the jar:

```bash
cd apriltag-generation
ant
```

This compiles `src/april/tag/*.java` into `build/` and packs `april.jar`
(confirmed: `BUILD SUCCESSFUL`, jar produced at repo root). Re-run `ant`
any time you add a new generated `TagFamily` class — see §5.

## 3. The `custom_<string>` layout language

A layout is a **flat string of characters**, one per pixel, read left-to-right
top-to-bottom, whose length must be a perfect square (`size = sqrt(len)`).
Pass it as `custom_<string>` to `TagFamilyGenerator`.

| char | meaning |
|---|---|
| `w` | white border pixel |
| `b` | black border pixel |
| `d` | data bit (this is where the payload lives) |
| `x` | not part of the tag (transparent / "hole") |
| `r` | recursive-tag pixel — **not implemented**, will throw at parse time |

`ImageLayout.Factory.createFromString` (`src/april/tag/ImageLayout.java:57`)
enforces two hard rules before it will accept a layout — get either wrong and
generation fails immediately with a clear exception:

1. **90°-rotational symmetry.** The grid must look the same under repeated
   90° rotation (necessary because the detector must recognize the tag at any
   orientation). Concretely: pixel `(x,y)` must have the same type as its
   rotated copies.
2. **A valid border ring.** Scanning outward from the tag, there must be one
   full pixel-ring of `w` immediately adjacent to a ring of `b` (standard) —
   or `b` outside `w` inside (this is the "reversed border" case, e.g. used by
   the `standard_x` layout, which puts data *outside* the border ring). If no
   such ring exists anywhere, you get `"Layout has no border."`

The bit count (`nbits`) is simply the number of `d` characters. `2^nbits` is
the full search space `TagFamilyGenerator` walks.

### Minimum working example (verified)

A 7×7 grid — 1px white border, 1px black border, a 3×3 data block in the
middle (9 data bits):

```
row0: wwwwwww
row1: wbbbbbw
row2: wbdddbw
row3: wbdddbw
row4: wbdddbw
row5: wbbbbbw
row6: wwwwwww
```

Flattened (49 chars, this is the exact string used in the walkthrough below):

```
wwwwwwwwbbbbbwwbdddbwwbdddbwwbdddbwwbbbbbwwwwwwww
```

The README's own example follows the same pattern for a classic-style tag
with a 4×4 data block:

```
custom_wwwwwwwwwbbbbbbwwbddddbwwbddddbwwbddddbwwbddddbwwbbbbbbwwwwwwwww
```

## 4. End-to-end walkthrough (all steps verified)

### Step 1 — Search for a codebook

```bash
java -cp april.jar april.tag.TagFamilyGenerator \
  custom_wwwwwwwwbbbbbwwbdddbwwbdddbwwbdddbwwbbbbbwwwwwwww 3
```

The second argument is the **minimum Hamming distance** between any two
accepted codes (accounting for rotation) — higher means more bit-error
tolerance at detection time but a smaller/harder-to-find codebook. This run
completed in ~30ms and found 5 codes:

```
public class TagCustom9h3 extends TagFamily
{
	private static class ConstructCodes0 {
		private static long[] constructCodes() {
			return new long[] { 0x1a6L, 0x16bL, 0x1ceL, 0x158L, 0x11dL };
		}
	}
	private static long[] constructCodes() {
		long[] codes = new long[5];
		System.arraycopy(ConstructCodes0.constructCodes(), 0, codes, 0, 5);
		return codes;
	}
	public TagCustom9h3()
	{
		super(ImageLayout.Factory.createFromString("Custom", "wwwwwwwwbbbbbwwbdddbwwbdddbwwbdddbwwbbbbbwwwwwwww"), 3, constructCodes());
	}
}
```

Note the printed class name is derived as `Tag<LayoutName><nbits>h<minHamming>`
— for `custom_...` inputs `LayoutName` is always `"Custom"`, giving names like
`TagCustom9h3`.

**Tuning notes** (observed directly):
- With `minHamming=5` on this same 9-bit layout, the search found **0
  codes** — too few bits for that much required separation. If you get an
  empty codebook, lower `minHamming` or use a bigger layout (more `d` pixels).
- Real families use far larger bit counts (e.g. `tagStandard41h12` has 41
  bits) — 9 bits here is only for a fast, legible demo. A real search over
  dozens of bits can take from seconds to hours depending on family size and
  CPU count (`TagFamilyGenerator` auto-parallelizes across all cores and
  prints a live ETA).

### Step 2 — Save it as a `.java` file and rebuild

Copy the printed class **verbatim** into `src/april/tag/<ClassName>.java`,
adding `package april.tag;` at the top (the generator's stdout omits the
package line the first time around a fresh report, but includes it — double
check your saved file has exactly one `package april.tag;` line):

```bash
cat > src/april/tag/TagCustom9h3.java << 'EOF'
package april.tag;

public class TagCustom9h3 extends TagFamily
{
	private static class ConstructCodes0 {
		private static long[] constructCodes() {
			return new long[] { 0x1a6L, 0x16bL, 0x1ceL, 0x158L, 0x11dL };
		}
	}
	private static long[] constructCodes() {
		long[] codes = new long[5];
		System.arraycopy(ConstructCodes0.constructCodes(), 0, codes, 0, 5);
		return codes;
	}
	public TagCustom9h3()
	{
		super(ImageLayout.Factory.createFromString("Custom", "wwwwwwwwbbbbbwwbdddbwwbdddbwwbdddbwwbbbbbwwwwwwww"), 3, constructCodes());
	}
}
EOF
ant
```

### Step 3 — Emit the C code

```bash
java -cp april.jar april.tag.TagToC april.tag.TagCustom9h3
```

Produces `tagCustom9h3.c` and `tagCustom9h3.h` in the current directory.
Verified contents (trimmed):

```c
// tagCustom9h3.h
apriltag_family_t *tagCustom9h3_create();
void tagCustom9h3_destroy(apriltag_family_t *tf);

// tagCustom9h3.c
static uint64_t codedata[5] = {
   0x00000000000001a6UL,
   0x000000000000016bUL,
   0x00000000000001ceUL,
   0x0000000000000158UL,
   0x000000000000011dUL,
};
apriltag_family_t *tagCustom9h3_create()
{
   apriltag_family_t *tf = calloc(1, sizeof(apriltag_family_t));
   tf->name = strdup("tagCustom9h3");
   tf->h = 3;
   tf->ncodes = 5;
   tf->codes = codedata;
   tf->nbits = 9;
   tf->bit_x = calloc(9, sizeof(uint32_t));   // + bit_y: the (x,y) sample
   ...                                          // location of every data bit
   tf->width_at_border = 5;
   tf->total_width = 7;
   ...
}
```

### Step 4 — Render printable tag images (optional)

```bash
java -cp april.jar april.tag.GenerateTags april.tag.TagCustom9h3 .
```

Produces one `.png` per code (`tag09_03_00000.png` … `tag09_03_00004.png`,
one per generated ID), plus `mosaic.png` and `alltags.ps` for print layout.
These come out tiny (the layout is only 7×7 pixels) — scale them up before
printing, e.g.:

```bash
convert tag09_03_00000.png -scale 800% tag09_03_00000_big.png
```

### Step 5 — Drop into the detector repo

Copy the generated pair into the `apriltag` C repo root and rebuild it:

```bash
cp tagCustom9h3.c tagCustom9h3.h ../apriltag/
cd ../apriltag
cmake -B build -DCMAKE_BUILD_TYPE=Release && cmake --build build
```

Because `CMakeLists.txt` globs `tag*.c` automatically
(`file(GLOB TAG_FILES ... tag*.c ...)`), no CMake edits are needed — it's
picked up on the next build. Use it exactly like any built-in family:

```c
#include "tagCustom9h3.h"
...
apriltag_family_t *tf = tagCustom9h3_create();
apriltag_detector_add_family(td, tf);
```

## 5. Troubleshooting reference (all reproduced directly)

| Error / symptom | Cause | Fix |
|---|---|---|
| `RuntimeException: Layout string is not square.` | string length isn't a perfect square | pad/trim to `size*size` chars |
| `RuntimeException: Layout not symmetric.` | grid doesn't match itself under 90° rotation | mirror your pixel types into all 4 quadrants |
| `RuntimeException: Layout has no border.` | no contiguous `w`-then-`b` (or reversed) ring found | add a full 1px white ring + full 1px black ring somewhere in the grid |
| `RuntimeException: Not implemented.` | used `r` (recursive) pixel type | not supported by this generator version — remove `r` pixels |
| Codebook comes back with **0 codes** | `minHamming` too high for the bit count / layout too small | lower `minHamming` or increase `d` pixel count |
| Generation runs "forever" | large `nbits` (e.g. 40+) means `2^nbits` search space | expected — let it run (multi-hour for big real families) or reduce layout size for testing |

## 6. Reference: built-in layout generators for comparison

`LayoutUtil.java` shows how the three built-in layout kinds are just
pre-built `custom_`-style strings:

- `classic_N` — 1px white + 1px black border, `(N-4)²` data bits inside (old AprilTag 2 style).
- `standard_N` — border is 1 pixel further in, with a data ring sitting *outside* the border (`reversed_border`-style density boost — AprilTag 3's default).
- `circle_N` — same border idea but pixels outside a computed radius are marked `x` (nothing), giving a round tag.

Use these as templates when hand-writing your own `custom_<string>` layout.
