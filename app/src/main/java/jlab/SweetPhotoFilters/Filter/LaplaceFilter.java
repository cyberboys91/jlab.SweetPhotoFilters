/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package jlab.SweetPhotoFilters.Filter;

/**
 * Edge detection via the Laplacian operator.
 * @author Jerry Huxtable
 */
public class LaplaceFilter extends Filter {

    private void brightness(int[] row) {
        for (int i = 0; i < row.length; i++) {
            int rgb = row[i];
            int r = rgb >> 16 & 0xff;
            int g = rgb >> 8 & 0xff;
            int b = rgb & 0xff;
            row[i] = (r + g + b) / 3;
        }
    }

    @Override
    public int[] filter(int[] src, int w, int h) {
        width = w;
        height = h;

        int[] dst = new int[w * h];

        int[] row1 = null;
        int[] row2 = null;
        int[] row3 = null;
        int[] pixels = new int[width];
        row1 = getRGB(src, 0, width);
        row2 = getRGB(src, 0, width);
        brightness(row1);
        brightness(row2);
        for (int y = 0; y < height; y++) {
            if (y < height - 1) {
                row3 = getRGB(src, y + 1, width);
                brightness(row3);
            }
            pixels[0] = pixels[width - 1] = 0xff000000;//FIXME
            for (int x = 1; x < width - 1; x++) {
                int l1 = row2[x - 1];
                int l2 = row1[x];
                int l3 = row3[x];
                int l4 = row2[x + 1];

                int l = row2[x];
                int max = Math.max(Math.max(l1, l2), Math.max(l3, l4));
                int min = Math.min(Math.min(l1, l2), Math.min(l3, l4));

                int gradient = (int) (0.5f * Math.max((max - l), (l - min)));

                int r = ((row1[x - 1] + row1[x] + row1[x + 1] +
                        row2[x - 1] - (8 * row2[x]) + row2[x + 1] +
                        row3[x - 1] + row3[x] + row3[x + 1]) > 0) ?
                        gradient : (128 + gradient);
                pixels[x] = r;
            }
            setRGB(dst, y, width, pixels);
            int[] t = row1;
            row1 = row2;
            row2 = row3;
            row3 = t;
        }

        row1 = getRGB(dst, 0, width);
        row2 = getRGB(dst, 0, width);
        for (int y = 0; y < height; y++) {
            if (y < height - 1) {
                row3 = getRGB(dst, y + 1, width);
            }
            pixels[0] = pixels[width - 1] = 0xff000000;//FIXME
            for (int x = 1; x < width - 1; x++) {
                int r = row2[x];
                r = (((r <= 128) &&
                        ((row1[x - 1] > 128) ||
                                (row1[x] > 128) ||
                                (row1[x + 1] > 128) ||
                                (row2[x - 1] > 128) ||
                                (row2[x + 1] > 128) ||
                                (row3[x - 1] > 128) ||
                                (row3[x] > 128) ||
                                (row3[x + 1] > 128))) ?
                        ((r >= 128) ? (r - 128) : r) : 0);

                pixels[x] = 0xff000000 | (r << 16) | (r << 8) | r;
            }
            setRGB(dst, y, width, pixels);
            int[] t = row1;
            row1 = row2;
            row2 = row3;
            row3 = t;
        }

        return dst;
    }

    private int[] getRGB(int[] src, int y, int width) {
        int[] ret = new int[width];
        int index = 0;
        for (int i = (y * width); i < (y * width) + width; ++i) {
            ret[index] = src[i];
            index++;
        }
        return ret;
    }

    private void setRGB(int[] dst, int y, int width, int[] src) {
        int index = 0;
        for (int i = (y * width); i < (y * width) + width; ++i) {
            dst[i] = src[index];
            index++;
        }
    }

    public String toString() {
        return "Edges/Laplace...";
    }
}
