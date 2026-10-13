package april.tag;

public class TagCustom16h5 extends TagFamily
{
	private static class ConstructCodes0 {
		private static long[] constructCodes() {
			return new long[] { 0x9765L, 0x9d2aL, 0xa2efL, 0xa8b4L, 0xbfc8L, 0xf3b5L, 0x2d67L, 0x55caL, 0xed49L, 0xb691L, 0xef9cL, 0xac3bL, 0xaf35L, 0x93d9L, 0xdd14L, 0x8ba7L, 0x6ae4L, 0xe5b9L, 0x6b71L };
		}
	}

	private static long[] constructCodes() {
		long[] codes = new long[19];
		System.arraycopy(ConstructCodes0.constructCodes(), 0, codes, 0, 19);
		return codes;
	}

	public TagCustom16h5()
	{
		super(ImageLayout.Factory.createFromString("Custom", "wwwwwwwwwwbbbbbbbwwbdddddbwwbdxxxdbwwbdxxxdbwwbdxxxdbwwbdddddbwwbbbbbbbwwwwwwwwww"), 5, constructCodes());
	}
}
