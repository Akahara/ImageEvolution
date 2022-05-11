package fr.wonder.iev;

import java.awt.Graphics;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import fr.wonder.commons.files.FilesUtils;

public class SpritemapGenerator {

	public static void main(String[] args) throws IOException {
		File dir = new File("sprites");
		File outputImg = new File("sprites.png");
		File outputData = new File("sprites.txt");
		
		if(!dir.exists()) {
			dir.mkdir();
			return;
		}
		
		List<BufferedImage> sprites = new ArrayList<>();
		int imgWidth = 0, imgHeight = 0;
		for(File f : dir.listFiles()) {
			if(!f.isFile() || f.getName().startsWith("_"))
				continue;
			BufferedImage sprite = ImageIO.read(f);
			sprites.add(sprite);
			imgWidth += sprite.getWidth();
			imgHeight = Math.max(imgHeight, sprite.getHeight());
		}
		
		if(imgWidth == 0 || imgHeight == 0)
			return;
		
		BufferedImage img = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
		StringBuilder sb = new StringBuilder();
		
		Graphics graphics = img.getGraphics();
		int x = 0;
		for(int i = 0; i < sprites.size(); i++) {
			BufferedImage sprite = sprites.get(i);
            ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
            op.filter(sprite, sprite);
			graphics.drawImage(sprite, x, 0, null);
			sb.append(x/(float) imgWidth);                       sb.append(":");
			sb.append(0/(float) imgHeight);                      sb.append(":");
			sb.append((x+sprite.getWidth())/(float) imgWidth);   sb.append(":");
			sb.append((0+sprite.getHeight())/(float) imgHeight); sb.append(";");
			x += sprite.getWidth();
		}
		graphics.dispose();
		
		ImageIO.write(img, "png", outputImg);
		FilesUtils.write(outputData, sb.toString());
	}
	
}
