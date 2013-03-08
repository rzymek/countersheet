package cs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class CounterSheet {
	private static final String FORMAT = "png";
	private final List<BufferedImage> singleSided = new ArrayList<BufferedImage>();
	private final List<String> singleSidedNames = new ArrayList<String>();
	private final List<BufferedImage> fronts = new ArrayList<BufferedImage>();
	private final List<BufferedImage> backs = new ArrayList<BufferedImage>();
	private final File imgDir;
	private final Dimension counterDim;

	public CounterSheet(File imgDir, String index) throws Exception {
		this.imgDir = imgDir;
		loadFiles(index);
		counterDim = findMaxDim();
	}

	private Dimension findMaxDim() {
		ArrayList<BufferedImage> all = new ArrayList<BufferedImage>();
		all.addAll(singleSided);
		all.addAll(fronts);
		all.addAll(backs);
		Dimension dim = new Dimension(-1, -1);
		for (BufferedImage img : all) {
			dim.width = Math.max(dim.width, img.getWidth());
			dim.height = Math.max(dim.height, img.getHeight());
		}
		return dim;
	}

	private void loadFiles(String string) throws Exception {
		FileReader fread = new FileReader(string);
		BufferedReader in = new BufferedReader(fread);
		try {
			for (;;) {
				String line = in.readLine();
				if (line == null) {
					break;
				}
				String[] split = line.split(":");
				String front = split[0].trim();
				String back = split.length > 1 ? split[1].trim() : "";
				if (back.isEmpty()) {
					BufferedImage img = load(front);
					singleSided.add(img);
					singleSidedNames.add(front);
				} else {
					fronts.add(load(front));
					backs.add(load(back));
				}
			}
		} finally {
			in.close();
		}
	}

	private BufferedImage load(String path) throws IOException {
		return ImageIO.read(new File(imgDir, path));
	}

	public void saveFront(File dir, int columns) throws IOException {
		{
			List<BufferedImage> side = new ArrayList<BufferedImage>();
			side.addAll(fronts);
			side.addAll(singleSided);
			System.out.println("Total counters: "+side.size());
			BufferedImage img = draw(columns, side);
			ImageIO.write(img, FORMAT, new File(dir,"1-front."+FORMAT));
		}
		{
			BufferedImage img = drawDesc(singleSided, singleSidedNames);
			ImageIO.write(img, FORMAT, new File(dir,"5-single."+FORMAT));			
		}
		{
			List<BufferedImage> ordered = reorder(backs, columns);
			BufferedImage img = draw(columns, ordered);
			ImageIO.write(img, FORMAT, new File(dir,"3-back."+FORMAT));
			img.getGraphics().drawImage(img, img.getWidth(),0, 0, img.getHeight(),  
					0,0,img.getWidth(), img.getHeight(), null);
			ImageIO.write(img, FORMAT, new File(dir,"0-check-reorder."+FORMAT));
			img = draw(columns, backs);
			ImageIO.write(img, FORMAT, new File(dir,"2-check-positions."+FORMAT));
		}
	}
		
	private BufferedImage drawDesc(List<BufferedImage> img, List<String> desc) {		
		int width = 600;
		int height = img.size() * counterDim.height;
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics g = result.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
		g.setColor(Color.BLACK);
		g.setFont(g.getFont().deriveFont(counterDim.height/4f));
		for (int i = 0; i < img.size(); i++) {
			BufferedImage im = img.get(i);
			int y = i * counterDim.height;
			g.drawImage(im, 0, y, null);
			g.drawString(desc.get(i), counterDim.width+10, y+counterDim.height/2);			
		}
		return result;
	}

	private List<BufferedImage> reorder(List<BufferedImage> backs, int columns) {		
		List<BufferedImage> ordered = new ArrayList<BufferedImage>();
		int row = -1;
		for (int i = 0; i < backs.size(); i++) {
			if(i % columns == 0){
				row++;
			}
			int index = (row * columns) + (columns - i % columns) - 1;
			if(index >= backs.size()) {
				continue;
			}
			BufferedImage tmp = backs.get(index);
			ordered.add(tmp);			
		}
		return ordered;
	}

	private BufferedImage draw(int columns, List<BufferedImage> side1) {
		int rows = getRows(columns);
		int width = columns * counterDim.width;
		int height = rows * counterDim.height;
		BufferedImage sheet = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics g = sheet.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, width, height);
		int x=0;
		int y=0;
		for (BufferedImage img : side1) {
			g.drawImage(img, x, y, counterDim.width, counterDim.height, null);
			x += counterDim.width;
			if(x + counterDim.width > width) {
				x = 0;
				y += counterDim.height;
			}
		}
		return sheet;
	}

	private int getRows(int columns) {
		return (int) Math.ceil((singleSided.size() + fronts.size()) / (double) columns);
	}
	public static void main(String[] args) throws Exception {
		File dir = new File("mods/red-winter/images");
		CounterSheet cs = new CounterSheet(dir, "idx");
		cs.saveFront(new File("."), 11);
	}}
