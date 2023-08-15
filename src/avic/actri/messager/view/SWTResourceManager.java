/**
 * @copyright actri.avic
 */
package avic.actri.messager.view;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;

/**
 * @author tdan 2010-10-08
 */
@SuppressWarnings({ "rawtypes" })
public class SWTResourceManager {

	public static void dispose() {
		disposeColors();
		disposeFonts();
		disposeImages();
		disposeCursors();
	}

	private static HashMap<RGB, Color> m_ColorMap = new HashMap<RGB, Color>();

	public static Color getColor(int systemColorID) {
		Display display = Display.getCurrent();
		return display.getSystemColor(systemColorID);
	}

	public static Color getColor(int r, int g, int b) {
		return getColor(new RGB(r, g, b));
	}

	public static Color getColor(RGB rgb) {
		Color color = m_ColorMap.get(rgb);
		if (color == null) {
			Display display = Display.getCurrent();
			color = new Color(display, rgb);
			m_ColorMap.put(rgb, color);
		}
		return color;
	}

	public static void disposeColors() {
		for (Object element : m_ColorMap.values())
			((Color) element).dispose();
		m_ColorMap.clear();
	}

	private static HashMap<String, Image> m_ClassImageMap = new HashMap<String, Image>();

	private static HashMap<Image, HashMap<Image, Image>> m_ImageToDecoratorMap = new HashMap<Image, HashMap<Image, Image>>();

	protected static Image getImage(InputStream is) {
		Display display = Display.getCurrent();
		ImageData data = new ImageData(is);
		if (data.transparentPixel > 0)
			return new Image(display, data, data.getTransparencyMask());
		return new Image(display, data);
	}

	public static Image getImage(String path) {
		return getImage("default", path); //$NON-NLS-1$
	}

	public static Image getImage(String section, String path) {
		String key = section + '|' + SWTResourceManager.class.getName() + '|'
				+ path;
		Image image = m_ClassImageMap.get(key);
		if (image == null) {
			try {
				FileInputStream fis = new FileInputStream(path);
				image = getImage(fis);
				m_ClassImageMap.put(key, image);
				fis.close();
			} catch (IOException e) {
				image = getMissingImage();
				m_ClassImageMap.put(key, image);
			}
		}
		return image;
	}

	public static Image getImage(Class clazz, String path) {
		String key = clazz.getName() + '|' + path;
		Image image = m_ClassImageMap.get(key);
		if (image == null) {
			try {
				if (path.length() > 0 && path.charAt(0) == '/') {
					String newPath = path.substring(1, path.length());
					image = getImage(new BufferedInputStream(clazz
							.getClassLoader().getResourceAsStream(newPath)));
				} else {
					image = getImage(clazz.getResourceAsStream(path));
				}
				m_ClassImageMap.put(key, image);
			} catch (Exception e) {
				image = getMissingImage();
				m_ClassImageMap.put(key, image);
			}
		}
		return image;
	}

	private static final int MISSING_IMAGE_SIZE = 10;

	private static Image getMissingImage() {
		Image image = new Image(Display.getCurrent(), MISSING_IMAGE_SIZE,
				MISSING_IMAGE_SIZE);
		//
		GC gc = new GC(image);
		gc.setBackground(getColor(SWT.COLOR_RED));
		gc.fillRectangle(0, 0, MISSING_IMAGE_SIZE, MISSING_IMAGE_SIZE);
		gc.dispose();
		//
		return image;
	}

	public static final int TOP_LEFT = 1;
	public static final int TOP_RIGHT = 2;
	public static final int BOTTOM_LEFT = 3;
	public static final int BOTTOM_RIGHT = 4;

	public static Image decorateImage(Image baseImage, Image decorator) {
		return decorateImage(baseImage, decorator, BOTTOM_RIGHT);
	}

	public static Image decorateImage(Image baseImage, Image decorator,
			int corner) {
		HashMap<Image, Image> decoratedMap = m_ImageToDecoratorMap
				.get(baseImage);
		if (decoratedMap == null) {
			decoratedMap = new HashMap<Image, Image>();
			m_ImageToDecoratorMap.put(baseImage, decoratedMap);
		}
		Image result = decoratedMap.get(decorator);
		if (result == null) {
			Rectangle bid = baseImage.getBounds();
			Rectangle did = decorator.getBounds();
			result = new Image(Display.getCurrent(), bid.width, bid.height);
			GC gc = new GC(result);
			gc.drawImage(baseImage, 0, 0);
			//
			if (corner == TOP_LEFT) {
				gc.drawImage(decorator, 0, 0);
			} else if (corner == TOP_RIGHT) {
				gc.drawImage(decorator, bid.width - did.width - 1, 0);
			} else if (corner == BOTTOM_LEFT) {
				gc.drawImage(decorator, 0, bid.height - did.height - 1);
			} else if (corner == BOTTOM_RIGHT) {
				gc.drawImage(decorator, bid.width - did.width - 1, bid.height
						- did.height - 1);
			}
			//
			gc.dispose();
			decoratedMap.put(decorator, result);
		}
		return result;
	}

	public static void disposeImages() {
		for (Object element : m_ClassImageMap.values())
			((Image) element).dispose();
		m_ClassImageMap.clear();
		//
		for (Object element : m_ImageToDecoratorMap.values()) {
			HashMap<?, ?> decoratedMap = (HashMap<?, ?>) element;
			for (Object element2 : decoratedMap.values()) {
				Image image = (Image) element2;
				image.dispose();
			}
		}
	}

	public static void disposeImages(String section) {
		for (Iterator<String> I = m_ClassImageMap.keySet().iterator(); I
				.hasNext();) {
			String key = I.next();
			if (!key.startsWith(section + '|'))
				continue;
			Image image = m_ClassImageMap.get(key);
			image.dispose();
			I.remove();
		}
	}

	private static HashMap<String, Font> m_FontMap = new HashMap<String, Font>();

	private static HashMap<Font, Font> m_FontToBoldFontMap = new HashMap<Font, Font>();

	public static Font getFont(String name, int height, int style) {
		return getFont(name, height, style, false, false);
	}

	public static Font getFont(String name, int size, int style,
			boolean strikeout, boolean underline) {
		String fontName = name + '|' + size + '|' + style + '|' + strikeout
				+ '|' + underline;
		Font font = m_FontMap.get(fontName);
		if (font == null) {
			FontData fontData = new FontData(name, size, style);
			if (strikeout || underline) {
				try {
					Class<?> logFontClass = Class
							.forName("org.eclipse.swt.internal.win32.LOGFONT"); //$NON-NLS-1$
					Object logFont = FontData.class
							.getField("data").get(fontData); //$NON-NLS-1$
					if (logFont != null && logFontClass != null) {
						if (strikeout) {
							logFontClass
									.getField("lfStrikeOut").set(logFont, Byte.valueOf((byte) 1)); //$NON-NLS-1$
						}
						if (underline) {
							logFontClass
									.getField("lfUnderline").set(logFont, Byte.valueOf((byte) 1)); //$NON-NLS-1$
						}
					}
				} catch (Throwable e) {
					System.err
							.println("Unable to set underline or strikeout" + " (probably on a non-Windows platform). " + e); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			font = new Font(Display.getCurrent(), fontData);
			m_FontMap.put(fontName, font);
		}
		return font;
	}

	public static Font getFont(FontData fontData) {
		if (fontData == null) {
			return null;
		}
		String fontName = fontData.toString();
		Font font = m_FontMap.get(fontName);
		if (font == null) {
			font = new Font(Display.getCurrent(), fontData);
			m_FontMap.put(fontName, font);
		}
		return font;
	}

	public static Font getBoldFont(Font baseFont) {
		Font font = m_FontToBoldFontMap.get(baseFont);
		if (font == null) {
			FontData fontDatas[] = baseFont.getFontData();
			FontData data = fontDatas[0];
			font = new Font(Display.getCurrent(), data.getName(),
					data.getHeight(), SWT.BOLD);
			m_FontToBoldFontMap.put(baseFont, font);
		}
		return font;
	}

	public static void disposeFonts() {
		for (Iterator<Font> iter = m_FontMap.values().iterator(); iter
				.hasNext();)
			iter.next().dispose();
		m_FontMap.clear();
	}

	public static void fixCoolBarSize(CoolBar bar) {
		CoolItem[] items = bar.getItems();
		// ensure that each item has control (at least empty one)
		for (int i = 0; i < items.length; i++) {
			CoolItem item = items[i];
			if (item.getControl() == null)
				item.setControl(new Canvas(bar, SWT.NONE) {
					@Override
					public Point computeSize(int wHint, int hHint,
							boolean changed) {
						return new Point(20, 20);
					}
				});
		}
		// compute size for each item
		for (int i = 0; i < items.length; i++) {
			CoolItem item = items[i];
			Control control = item.getControl();
			control.pack();
			Point size = control.getSize();
			item.setSize(item.computeSize(size.x, size.y));
		}
	}

	private static HashMap<Integer, Cursor> m_IdToCursorMap = new HashMap<Integer, Cursor>();

	public static Cursor getCursor(int id) {
		Integer key = Integer.valueOf(id);
		Cursor cursor = m_IdToCursorMap.get(key);
		if (cursor == null) {
			cursor = new Cursor(Display.getDefault(), id);
			m_IdToCursorMap.put(key, cursor);
		}
		return cursor;
	}

	public static void disposeCursors() {
		for (Iterator<Cursor> iter = m_IdToCursorMap.values().iterator(); iter
				.hasNext();)
			iter.next().dispose();
		m_IdToCursorMap.clear();
	}
}