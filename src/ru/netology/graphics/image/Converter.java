package ru.netology.graphics.image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URL;


public class Converter implements TextGraphicsConverter {
    protected TextColorSchema schema = new ColorSchema();
    private int maxWidth;
    private int maxHeight;
    private double maxRatio;

    @Override
    public String convert(String url) throws IOException, BadImageSizeException {

        BufferedImage img = ImageIO.read(new URL(url)); // Скачиваем картинку из интернета
        int newWidth = img.getWidth(); // получаем значение ширины, позже изменяем в соответствии с пропорциями
        int newHeight = img.getHeight(); // получаем значение высоты, позже изменяем в соответствии с пропорциями
        checkMaxRatio(maxRatio, newWidth, newHeight);
        int[] newSize = resizeImage(newWidth, newHeight);
        Image scaledImage = img.getScaledInstance(newSize[0], newSize[1], BufferedImage.SCALE_SMOOTH); // Сузить картинку в соответствии с пропорциями

        BufferedImage bwImg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_GRAY); // Сделаем ее черно-белой
        Graphics2D graphics = bwImg.createGraphics(); // инструмент для рисования на картинке
        graphics.drawImage(scaledImage, 0, 0, null); // копировать содержимое из суженой картинки
        ImageIO.write(bwImg, "png", new File("out.png")); // сохраняем промежуточную картинку
        WritableRaster bwRaster = bwImg.getRaster(); // пройтись по пикселям

        StringBuilder imageText = new StringBuilder();

        for (int line = 0; line < newSize[1]; line++) {
            for (int row = 0; row < newSize[0]; row++) {
                int color = bwRaster.getPixel(row, line, new int[3])[0];
                char c = schema.convert(color);
                imageText.append(c);
                imageText.append(c);
            }
            imageText.append("\n");
        }
        String totalResult = imageText.toString();
        return totalResult;
    }

    @Override
    public void setMaxWidth(int width) {
        this.maxWidth = width;
    }

    @Override
    public void setMaxHeight(int height) {
        this.maxHeight = height;
    }

    @Override
    public void setMaxRatio(double maxRatio) {
        this.maxRatio = maxRatio;
    }

    @Override
    public void setTextColorSchema(TextColorSchema schema) {
        this.schema = schema;
    }

    public int[] resizeImage(int width, int height) {
        if (width < maxWidth && height < maxHeight) {
            return new int[]{width, height};
        }
        int maxSide = Math.max(height, width);
        int scaleSide = maxSide - maxWidth;
        double percent = (double) scaleSide / maxSide * 100;
        double t;
        if (width > height) {
            width = maxWidth;
            t = (height - (height * (percent / 100)));
            height = (int) t;
        } else {
            height = maxHeight;
            t = (width - (width * (percent / 100)));
            width = (int) t;
        }
        return new int[]{width, height};
    }

    public void checkMaxRatio(double maxRatio, int width, int height) throws BadImageSizeException {
        int maxSize = Math.max(width, height);
        int minSize = Math.min(width, height);
        double checkRatio = (double) maxSize / minSize;
        if (checkRatio > maxRatio) {
            throw new BadImageSizeException(checkRatio, maxRatio);
        }
    }
}

