package com.hse;

import javax.imageio.ImageIO;
import javax.print.DocFlavor;
import java.awt.image.BufferedImage;
import java.io.*;

import static com.hse.Options.ascii_palette;
import static com.hse.Options.redweight;
import static com.hse.Options.blueweight;
import static com.hse.Options.greenweight;

import static com.hse.ExceptionHandler.PrintException;

class RGB {
    int red;
    int green;
    int blue;
    int grey;
}



public class Image {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final int ANSI_BLACK = 38;
    public static final int ANSI_RED = 31;
    public static final int ANSI_GREEN = 32;
    public static final int ANSI_YELLOW = 33;
    public static final int ANSI_BLUE = 34;
    public static final int ANSI_PURPLE = 35;
    public static final int ANSI_CYAN = 36;
    public static final int ANSI_GREY = 37;
    public static final int ANSI_WHITE = 30;

    BufferedImage _Image;
    int width;
    int height;
    private boolean isImageReady = false;

    public boolean ReadFromDisk(String path) {
        try {
            _Image = ImageIO.read(new File(path));
            width = _Image.getWidth();
            height = _Image.getHeight();
            isImageReady = true;
            return true;
        }
        catch (IOException e) {
            PrintException("Couldn't read the file " + path, "Image");
            isImageReady = false;
            return false;
        }
    }

    private RGB GetFormattedPixel(int x, int y) {
        int rgb = _Image.getRGB(x, y);
        RGB pixel = new RGB();
        pixel.red = (rgb >> 16) & 0xFF;
        pixel.green = (rgb >> 8) & 0xFF;
        pixel.blue = rgb & 0xFF;
        pixel.grey = (int)(pixel.blue * blueweight + pixel.green * greenweight + pixel.red * redweight);
        return pixel;
    }

    public boolean PrintImageToConsole() {
        if (!isImageReady) {
            PrintException("Image not ready ", "Image");
            return false;
        }
        for (int j = 0; j < height; ++j) {
            for (int i = 0; i < width; ++i) {
                RGB current = GetFormattedPixel(i, j);
                int index = (int)(current.grey * (ascii_palette.length() - 1) / 255f);
                System.out.print(ascii_palette.charAt(index));
                System.out.print(ascii_palette.charAt(index));
            }
            System.out.print('\n');
        }
        return true;
    }

    public boolean PrintImageToFile(String path, boolean isDoubled) {
        if (!isImageReady) {
            PrintException("Image not ready ", "Image");
            return false;
        }
        PrintStream writer = null;

        try {
            writer = new PrintStream(new FileOutputStream(path));
            PrintImageToFile(writer, isDoubled);
            try {writer.close();} catch (Exception ex_) {/*ignore*/}
        } catch (IOException ex) {
            PrintException("Couldn't write into file " + path, "Image");
            try {writer.close();} catch (Exception ex_) {/*ignore*/}
            return false;
        }

        return true;
    }

    public boolean PrintImageToFile(PrintStream writer, boolean isDoubled) {
        if (!isImageReady) {
            PrintException("Image not ready ", "Image");
            return false;
        }

            for (int j = 0; j < height; ++j) {
                String str = "";
                for (int i = 0; i < width; ++i) {
                    RGB current = GetFormattedPixel(i, j);
                    int index = (int)(current.grey * (ascii_palette.length() - 1) / 255f);
                    str = str + ascii_palette.charAt(index) + (isDoubled ? ascii_palette.charAt(index) : "");
                }
                str = str + '\n';
                writer.print(str);
            }

        return true;
    }

    public boolean PrintColoredImageToFile(String path, boolean fill, boolean isSpace, boolean isDoubled) {
        if (!isImageReady) {
            PrintException("Image not ready ", "Image");
            return false;
        }
        try {
            PrintStream writer = new PrintStream( new FileOutputStream(new File(path)));
            return PrintColoredImageToFile(writer, fill, isSpace, isDoubled);
        } catch (IOException ex) {
            PrintException("Couldn't write into given file ", "Image");
            return false;
        }
    }

    public boolean PrintColoredImageToFile(File F, boolean fill, boolean isSpace, boolean isDoubled) {
        if (!isImageReady) {
            PrintException("Image not ready ", "Image");
            return false;
        }
        try {
            PrintStream writer = new PrintStream(new PrintStream(
                new FileOutputStream(F)));
            PrintColoredImageToFile(writer, fill, isSpace, isDoubled);
        } catch (IOException ex) {
            PrintException("Couldn't write into given file ", "Image");
            return false;
        }

        return true;
    }

    public boolean PrintColoredImageToFile(PrintStream F, boolean fill, boolean isSpace, boolean isDoubled) {
        if (!isImageReady) {
            PrintException("Image not ready ", "Image");
            return false;
        }

            for (int j = 0; j < height; ++j) {
                String str = "";
                for (int i = 0; i < width; ++i) {
                    RGB current = GetFormattedPixel(i, j);
                    int index = (int)(current.grey * (ascii_palette.length() - 1) / 255f);

                    float t = 0.01f * 255; // threshold
                    int colr = ANSI_BLACK;
                    int R = current.red;
                    int G = current.green;
                    int B = current.blue;
                    int Y = current.grey;
                    char ch = ascii_palette.charAt(index);
                    if ( R-t>G && R-t>B )                 colr = ANSI_RED; // red
                    else if (R + G + B < t)               colr = ANSI_BLACK;
                    else if ( G-t>R && G-t>B )            colr = ANSI_GREEN; // green
                    else if ( R-t>B && G-t>B && R+G>i )   colr = ANSI_YELLOW; // yellow
                    else if ( B-t>R && B-t>G )            colr = ANSI_BLUE; // blue
                    else if ( R-t>G && B-t>G && R+B>i )   colr = ANSI_PURPLE; // magenta
                    else if ( G-t>R && B-t>R && B+G>i )   colr = ANSI_CYAN; // cyan
                    else if ( R+G+B>=3.0f*Y )             colr = ANSI_WHITE; // white
                    //else              colr = ANSI_WHITE; // white
                    if (isSpace)
                        ch = ' ';
                    if (fill)
                        colr += 10;
                    String formatted = String.format("\u001B[%dm", colr);
                    str = str + formatted + ch + (isDoubled ? ch : "") + ANSI_RESET;
                }
                //str = str + '\n';
                F.println(str);
            }



        return true;
    }

    public boolean ConvertToAscii(String path) {
        if (!ReadFromDisk(path)) {
            PrintException("Applications Crashed", "Image");
            return false;
        }

        return true;
    }


}
