package april.tag;

public class TagCustom12h5 extends TagFamily
{
	private static class ConstructCodes0 {
		private static long[] constructCodes() {
			return new long[] { 0x564L, 0xfdcL, 0xf53L, 0xa74L };
		}
	}

	private static long[] constructCodes() {
		long[] codes = new long[4];
		System.arraycopy(ConstructCodes0.constructCodes(), 0, codes, 0, 4);
		return codes;
	}

	public TagCustom12h5()
	{
		super(ImageLayout.Factory.createFromString("Custom", "wwwwwwwwwbbbbbbwwbddddbwwbdxxdbwwbdxxdbwwbddddbwwbbbbbbwwwwwwwww"), 5, constructCodes());
	}
}
