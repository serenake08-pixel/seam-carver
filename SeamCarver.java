import edu.princeton.cs.algs4.Picture;
import java.awt.Color;
import java.lang.Math;

public class SeamCarver {
    private Picture picture;
    private int width;
    private int height;

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null) {
            throw new IllegalArgumentException("null");
        }
        this.picture = new Picture(picture);
        width = picture.width();
        height = picture.height();
    }

    // current picture
    public Picture picture() {
        return new Picture(picture);
    }

    // width of current picture
    public int width() {
        return width;
    }

    // height of current picture
    public int height() {
        return height;
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        if (x < 0 || y < 0 || x >= width() || y >= height()) {
            throw new IllegalArgumentException("not within range");
        }
        if (x == 0 || y == 0) {
            return 1000;
        }
        if (x == width() - 1 || y == height() - 1) {
            return 1000;
        }

        Color top = picture.get(x, y - 1);
        Color bottom = picture.get(x, y + 1);
        Color left = picture.get(x - 1, y);
        Color right = picture.get(x + 1, y);

        // rbg differences squared
        int rX2 = (int) Math.pow(right.getRed() - left.getRed(), 2);
        int bX2 = (int) Math.pow(right.getBlue() - left.getBlue(), 2);
        int gX2 = (int) Math.pow(right.getGreen() - left.getGreen(), 2);

        // sum of those differences
        int x2 = rX2 + bX2 + gX2;

        // similarly for y
        int rY2 = (int) Math.pow(top.getRed() - bottom.getRed(), 2);
        int bY2 = (int) Math.pow(top.getBlue() - bottom.getBlue(), 2);
        int gY2 = (int) Math.pow(top.getGreen() - bottom.getGreen(), 2);

        int y2 = rY2 + bY2 + gY2;

        return Math.sqrt(x2 + y2);
    }

    // sequence of indices for horizontal seam
    // they are the x values
    public int[] findHorizontalSeam() {
        return findSeam(false);

    }

    // sequence of indices for vertical seam
    // they are the y values
    public int[] findVerticalSeam() {
        return findSeam(true);
    }

    private int[] findSeam(boolean byRow) {
        int tmpHeight = byRow ? height : width;
        int tmpWidth = byRow ? width : height;
        // some corner cases
        if (tmpHeight == 1) {
            return new int[] { 0 };
        } else if (tmpHeight == 2) {
            return new int[] { 0, 0 };
        }
        // tmpHeight > 2!!!!!

        double[][] distTo = new double[tmpHeight][tmpWidth]; // dist[h][w] = shortest dist (incl the vertex itself)
        int[][] edgeTo = new int[tmpHeight][tmpWidth];

        // fill out distTo and edgeTo
        for (int w = 0; w < tmpWidth; w++) {
            distTo[0][w] = 1000;
        }

        for (int h = 1; h < tmpHeight; h++) {
            for (int w = 0; w < tmpWidth; w++) {
                edgeTo[h][w] = findEdgeToV(h, w, distTo, byRow ? width : height);
                double energy = byRow ? energy(w, h) : energy(h, w);
                distTo[h][w] = distTo[h - 1][edgeTo[h][w]] + energy;
            }
        }

        // find shortest distTo from the bottom column
        double minDist = distTo[tmpHeight - 1][0];
        int minIndex = 0;
        for (int w = 0; w < tmpWidth; w++) {
            if (distTo[tmpHeight - 1][w] < minDist) {
                minDist = distTo[tmpHeight - 1][w];
                minIndex = w;
            }
        }

        // start filling it up!!!!!
        int[] seam = new int[tmpHeight];
        seam[tmpHeight - 1] = minIndex;
        for (int h = tmpHeight - 1; h > 0; h--) {
            seam[h - 1] = edgeTo[h][seam[h]];
        }

        return seam;
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        removeSeam(seam, false);
    }

    public void removeVerticalSeam(int[] seam) {
        removeSeam(seam, true);
    }

    // remove vertical seam from current picture
    private void removeSeam(int[] seam, boolean byRow) {
        validateSeam(seam, byRow);

        Picture newPicture = new Picture(width - (byRow ? 1 : 0), height - (byRow ? 0 : 1));
        if (byRow) {
            for (int h = 0; h < height; h++) {
                for (int w = 0; w < width; w++) {
                    int newW = w;
                    if (w == seam[h]) {
                        continue;
                    } else if (w >= seam[h]) {
                        newW--;
                    }
                    Color c = new Color(picture.get(w, h).getRGB());
                    newPicture.set(newW, h, c);
                }
            }
        } else {
            for (int w = 0; w < width; w++) {
                for (int h = 0; h < height; h++) {
                    int newH = h;
                    if (h == seam[w]) {
                        continue;
                    } else if (h >= seam[w]) {
                        newH--;
                    }
                    Color c = new Color(picture.get(w, h).getRGB());
                    newPicture.set(w, newH, c);
                }
            }
        }

        picture = newPicture;
        width = picture.width();
        height = picture.height();
    }

    private void validateSeam(int[] seam, boolean byRow){
        if (seam == null) {
            throw new IllegalArgumentException("null");
        }
        
        if (seam.length != (byRow? height : width)){
            throw new IllegalArgumentException("invalid seam length");
        }

        if (seam[0] >= (byRow? width : height) || seam[0] < 0){
            throw new IllegalArgumentException("invalid seam entry");
        }

        for (int i = 1; i < seam.length; i++){
            if (seam[i] >= (byRow? width : height) || seam[i] < 0){
                throw new IllegalArgumentException("invalid seam entry");
            }
            if (seam[i] - seam[i-1] != -1 && seam[i] != seam[i-1] && seam[i] - seam[i-1] != 1){
                throw new IllegalArgumentException("invalid seam entry"); 
            }
        }
    }

    // return w, w-1, or w+1
    private int findEdgeToV(int h, int w, double[][] distTo, int widthSize) {
        if (h <= 0) {
            throw new IllegalArgumentException("inputed h <= 0, but can't look at negative index");
        }
        if (h == 1) { // top row
            return w;
        } else if (w == 0 && w == widthSize - 1) { // left AND right border
            return w;
        } else if (w == 0) { // left border
            if (distTo[h - 1][w] < distTo[h - 1][w + 1]) {
                return w;
            } else {
                return w + 1;
            }
        } else if (w == widthSize - 1) { // right border
            if (distTo[h - 1][w - 1] < distTo[h - 1][w]) {
                return w - 1;
            } else {
                return w;
            }
        } else { // everything else
            if (distTo[h - 1][w - 1] < distTo[h - 1][w]) { // w-1 < w
                if (distTo[h - 1][w - 1] < distTo[h - 1][w + 1]) {
                    return w - 1;
                } else {
                    return w + 1;
                }
            } else { // w < w-1
                if (distTo[h - 1][w] < distTo[h - 1][w + 1]) {
                    return w;
                } else {
                    return w + 1;
                }
            }
        }
    }

    /*
     * private int[] findRandVerticalSeam() {
     * int[] seam = new int[height];
     * seam[0] = (int) (Math.random() * width);
     * 
     * for (int h = 1; h < height; h++) {
     * if (seam[h - 1] == 0) {
     * seam[h] = (int) (Math.random() * 2);
     * } else if (seam[h - 1] == width - 1) {
     * seam[h] = width - 1 - (int) (Math.random() * 2);
     * } else {
     * seam[h] = seam[h - 1] - 1 + (int) (Math.random() * 3);
     * }
     * }
     * 
     * return seam;
     * }
     */

    // unit testing (optional)

    public static void main(String[] args) {
        /*
         * Picture p = new Picture("HJoceanSmall.png");
         * p.show();
         * SeamCarver test = new SeamCarver(p);
         * for (int i = 0; i < 250; i++) {
         * test.removeVerticalSeam(test.findVerticalSeam());
         * }
         * test.picture.show();
         */

        Picture q = new Picture("HJoceanSmall.png");
        q.show();
        SeamCarver test = new SeamCarver(q);
        for (int i = 0; i < 100; i++) {
            test.removeHorizontalSeam(test.findHorizontalSeam());
        }
        test.picture.show();
    }
}