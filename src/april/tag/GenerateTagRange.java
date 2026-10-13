package april.tag;

import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class GenerateTagRange {
    public static void main(String args[]) throws IOException {
        if (args.length != 4) {
            System.out.printf("Usage: <tagclass> <outputdir> <idStart> <idEndInclusive>\n");
            return;
        }

        String cls = args[0];
        String dirpath = args[1] + "/";
        int idStart = Integer.parseInt(args[2]);
        int idEnd = Integer.parseInt(args[3]);

        TagFamily tagFamily = (TagFamily) april.util.ReflectUtil.createObject(cls);
        if (tagFamily == null) {
            System.err.println("Tag family not found.");
            return;
        }

        TagRenderer renderer = new TagRenderer(tagFamily);

        File f = new File(dirpath);
        if (!f.exists())
            f.mkdirs();

        for (int id = idStart; id <= idEnd; id++) {
            String path = String.format("%s%s_%05d.png", dirpath, tagFamily.getFilePrefix(), id);
            ImageIO.write(renderer.makeImage(id), "png", new File(path));
            System.out.println("wrote " + path);
        }
    }
}
