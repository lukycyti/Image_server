package pdl.backend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import boofcv.alg.color.ColorHsv;
import boofcv.struct.image.GrayI;
import boofcv.struct.image.GrayS16;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;

public class ImageProcessing {

  /**
   * Change la luminosité sur une image en couleur
   * 
   * @param input
   * @param delta intensité du changement
   */
  public static void brightnessChanger(Planar<GrayU8> input, Planar<GrayU8> output, int t) {
    for (int band = 0; band < input.getNumBands(); band++) {
      GrayU8 bandInput = input.getBand(band);
      GrayU8 bandOutput = output.getBand(band);
      for (int y = 0; y < bandInput.height; ++y) {
        for (int x = 0; x < bandInput.width; ++x) {
          int gl = bandInput.get(x, y) + t;
          if (gl > 255) {
            gl = 255;
          } else if (gl < 0) {
            gl = 0;
          }
          bandOutput.set(x, y, gl);
        }
      }
    }
  }

  /**
   * Color l'image en fonction de la teinte donnée
   * 
   * @param input
   * @param output
   * @param hue l'angle correspondant à la couleur voulue
   */
  public static void colorImage(Planar<GrayU8> input, Planar<GrayU8> output, int hue) throws WrongValueException {
    if (hue > 360 || hue < 0)
      throw new WrongValueException("La valeur de la teinte doit être entre 0 et 360");
    int numBands = input.getNumBands();
    for (int y = 0; y < input.height; ++y) {
      for (int x = 0; x < input.width; ++x) {
        int r, b, g;
        r = input.getBand(0).get(x, y);
        if (numBands < 2) {
          b = r;
          g = r;
        } else {
          g = input.getBand(1).get(x, y);
          b = input.getBand(2).get(x, y);
        }
        float[] hsv = new float[3];
        ColorHsv.rgbToHsv(r, g, b, hsv);
        float[] rgb = new float[3];
        float h = (float) (hue * Math.PI / 180.0);
        ColorHsv.hsvToRgb(h, hsv[1], hsv[2], rgb);

        output.getBand(0).set(x, y, (int) rgb[0]);
        if (numBands > 1) {
          output.getBand(1).set(x, y, (int) rgb[1]);
          output.getBand(2).set(x, y, (int) rgb[2]);
        }
      }
    }

  }

  /**
   * Converti une image couleur en niveau de gris
   * 
   * @param input
   * @param output
   */
  public static void convertColorToGray(Planar<GrayU8> input, Planar<GrayU8> output) {

    for (int y = 0; y < input.height; ++y) {
      for (int x = 0; x < input.width; ++x) {
        double ng = 0;
        for (int c = 0; c < input.getNumBands(); c++) {
          int val = input.getBand(c).get(x, y);
          switch (c) {
            case 0:
              ng += val * 0.3;
              break;
            case 1:
              ng += val * 0.59;
              break;
            case 2:
              ng += val * 0.11;
              break;
            default:
              System.out.println("erreur");
              break;
          }
        }

        for (int c = 0; c < input.getNumBands(); c++) {
          output.getBand(c).set(x, y, (int) ng);
        }
      }
    }
  }

  /**
   * Applique une convultion avec un kernel prédefini
   * @param input
   * @param output
   * @param kernel 
   */
  private static void convolution(GrayU8 input, GrayI<?> output, int[][] kernel) {
    int w = input.width;
    int h = input.height;
    int kernelL = kernel.length;
    int halfKernel = kernelL / 2;
    int sumKernel = 0;

    for (int y = 0; y < kernelL; y++) {
      for (int x = 0; x < kernelL; x++) {
        sumKernel += kernel[x][y];
      }
    }

    for (int y = halfKernel; y < h - halfKernel; y++) {
      for (int x = halfKernel; x < w - halfKernel; x++) {
        int sum = 0;
        for (int i = -halfKernel; i <= halfKernel; i++) {
          for (int j = -halfKernel; j <= halfKernel; j++) {
            int inputX = x + j;
            int inputY = y + i;
            int value = kernel[halfKernel + j][halfKernel + i];
            int inputValue = input.get(inputX, inputY);
            sum += inputValue * value;
          }
        }
        if (sumKernel != 0) {
          output.set(x, y, sum / sumKernel);
        } else {
          output.set(x, y, sum);
        }
      }
    }
  }

  /**
   * Affiche les contours de l'image
   * @param input
   * @param output
   */
  public static void showContours(Planar<GrayU8> input, Planar<GrayU8> output) {
    int[][] sobelX = { { -1, 0, 1 }, { -2, 0, 2 }, { -1, 0, 1 } };
    int[][] sobelY = { { -1, -2, -1 }, { 0, 0, 0 }, { 1, 2, 1 } };
    int w = input.width;
    int h = input.height;
    int max = 0;
    
    Planar<GrayU8> gray = input.createSameShape();
    convertColorToGray(input, gray);

    Planar<GrayS16> gradXS = new Planar<>(GrayS16.class, w, h, 3);
    Planar<GrayS16> gradYS = new Planar<>(GrayS16.class, w, h, 3);
    Planar<GrayS16> gradientS = new Planar<>(GrayS16.class, w, h, 3);

    for (int c = 0; c < input.getNumBands(); c++) {
      convolution(input.getBand(c), gradXS.getBand(c), sobelX);
      convolution(input.getBand(c), gradYS.getBand(c), sobelY);
    }

    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        int gx = 0, gy = 0;
        for (int c = 0; c < input.getNumBands(); c++) {
          gx += gradXS.getBand(c).get(x, y) / 4;
          gy += gradYS.getBand(c).get(x, y) / 4;
        }

        if (x == 0 || x == w - 1 || y == 0 || y == h - 1) {
          gradientS.getBand(0).set(x, y, 0);
          gradientS.getBand(1).set(x, y, 0);
          gradientS.getBand(2).set(x, y, 0);
        } else {
          int v = (int) Math.sqrt(gx * gx + gy * gy);
          if (v > max) {
            max = v;
          }
          gradientS.getBand(0).set(x, y, v);
          gradientS.getBand(1).set(x, y, v);
          gradientS.getBand(2).set(x, y, v);
        }
      }
    }

    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        int val = gradientS.getBand(0).get(x, y) * 255 / max;
        for (int c = 0; c < output.getNumBands(); c++) {
          output.getBand(c).set(x, y, val);
        }
      }
    }
  }

  /**
   * Applique une hégalisation d'histogramme sur l'image
   * @param input
   * @param output
   */
  public static void histogramEqualization(Planar<GrayU8> input, Planar<GrayU8> output) {
    int N = input.getHeight() * input.getWidth();

    for (int b = 0; b < input.getNumBands(); b++) {
      GrayU8 band = input.getBand(b);
      int[] h = new int[256];
      for (int y = 0; y < band.getHeight(); ++y) {
        for (int x = 0; x < band.getWidth(); ++x) {
          int gl = band.get(x, y);
          h[gl]++;
        }
      }

      int[] LUT = new int[256];
      for (int i = 0; i <= 255; i++) {
        int val = (c(i, h) * 255) / N;
        if (val > 255) {
          val = 255;
        } else if (val < 0) {
          val = 0;
        }
        LUT[i] = val;
      }

      GrayU8 outBand = output.getBand(b);
      for (int y = 0; y < band.getHeight(); ++y) {
        for (int x = 0; x < band.getWidth(); ++x) {
          outBand.set(x, y, LUT[band.get(x, y)]);
        }
      }

      output.setBand(b, outBand);
    }
  }

  public static int c(int i, int[] h) {
    int sum = 0;
    for (int j = 0; j <= i; j++) {
      sum += h[j];
    }
    return sum;
  }

  /**
   * Floute l'image
   * @param input
   * @param output
   * @param borderType Comportement vis à vis des bordures
   * @param size Intensité du floutage
   * @throws WrongValueException
   */
  public static void blurImage(Planar<GrayU8> input, Planar<GrayU8> output, int borderType,
      int size) throws WrongValueException {
    for (int c = 0; c < input.getNumBands(); c++) {
      meanFilterWithBorders(input.getBand(c), output.getBand(c), size, borderType);
    }
  }

  /**
   * Applique le filtre moyenneur
   * @param input
   * @param output
   * @param size Intensité du floutage
   * @param borderType Comportement vis à vis des bordures
   * @throws WrongValueException
   */
  private static void meanFilterWithBorders(GrayU8 input, GrayU8 output, int size, int borderType)
      throws WrongValueException {
    // SKIP : 0, NORMALIZED = 1, EXTENDED = 2, REFLECT = 3
    switch (borderType) {
      case 0:
        meanFilterSkip(input, output, size);
        break;
      case 1:
        meanFilterNormalized(input, output, size);
        break;
      case 2:
        meanFilterExtended(input, output, size);
        break;
      case 3:
        meanFilterReflect(input, output, size);
        break;
      default:
        throw new WrongValueException("Erreur valeur borderType (elle doit être entre 0 et 3)");
    }

  }

  /**
   * Applique le filtre moyenneur avec le comportement SKIP
   * @param input
   * @param output
   * @param size
   */
  private static void meanFilterSkip(GrayU8 input, GrayU8 output, int size) {
    int halfDelta = size / 2;
    for (int y = halfDelta; y < input.height - halfDelta; ++y) {
      for (int x = halfDelta; x < input.width - halfDelta; ++x) {
        int val = 0;
        int cpt = 0;
        for (int i = -halfDelta; i <= halfDelta; i++) {
          for (int j = -halfDelta; j <= halfDelta; j++) {
            val += input.get(i + x, j + y);
            cpt++;
          }
        }
        output.set(x, y, val / (cpt));
      }
    }

  }

  /**
   * Applique le filtre moyenneur avec le comportement NORMALIZED
   * @param input
   * @param output
   * @param size
   */
  private static void meanFilterNormalized(GrayU8 input, GrayU8 output, int size) {
    int halfDelta = size / 2;
    for (int y = 0; y < input.height; ++y) {
      for (int x = 0; x < input.width; ++x) {
        int val = 0;
        int cpt = 0;
        for (int i = -halfDelta; i <= halfDelta; i++) {
          for (int j = -halfDelta; j <= halfDelta; j++) {
            int w = i + x;
            int h = j + y;
            if (w > 0 && w < input.width && h > 0 && h < input.height) {
              val += input.get(w, h);
              cpt++;
            }
          }
        }
        output.set(x, y, val / (cpt));
      }
    }
  }


  /**
   * Applique le filtre moyenneur avec le comportement EXTENDED
   * @param input
   * @param output
   * @param size
   */
  private static void meanFilterExtended(GrayU8 input, GrayU8 output, int size) {
    int halfDelta = size / 2;
    for (int y = 0; y < input.height; ++y) {
      for (int x = 0; x < input.width; ++x) {
        int val = 0;
        int cpt = 0;
        for (int i = -halfDelta; i <= halfDelta; i++) {
          for (int j = -halfDelta; j <= halfDelta; j++) {
            int w = i + x;
            int h = j + y;
            if (w > 0 && w < input.width && h > 0 && h < input.height) {
              val += input.get(w, h);

            } else {
              if (w < 0) {
                w = 0;
              }
              if (h < 0) {
                h = 0;
              }
              if (w >= input.width) {
                w = input.width - 1;
              }
              if (h >= input.height) {
                h = input.height - 1;
              }
              val += input.get(w, h);
            }
            cpt++;
          }
        }
        output.set(x, y, val / (cpt));
      }
    }
  }
  
  /**
   * Applique le filtre moyenneur avec le comportement REFLECT
   * @param input
   * @param output
   * @param size
   */
  private static void meanFilterReflect(GrayU8 input, GrayU8 output, int size) {
    int halfDelta = size / 2;
    for (int y = 0; y < input.height; ++y) {
      for (int x = 0; x < input.width; ++x) {
        int val = 0;
        int cpt = 0;
        for (int i = -halfDelta; i <= halfDelta; i++) {
          for (int j = -halfDelta; j <= halfDelta; j++) {
            int w = Math.abs(i + x);
            int h = Math.abs(j + y);
            if (w >= input.width) {
              w = w - i;
            }
            if (h >= input.height) {
              h = h - j;
            }
            val += input.get(w, h);
            cpt++;
          }
        }
        output.set(x, y, val / (cpt));

      }
    }
  }

  /**
   * Inverse les couleurs d'image en appliquant le filtre négatif
   * @param input
   * @param output
   */
  public static void negativeFilter(Planar<GrayU8> input, Planar<GrayU8> output) {
    for (int y = 0; y < input.height; ++y) {
      for (int x = 0; x < input.width; ++x) {
        for (int band = 0; band < input.getNumBands(); band++) {
          int newValue = input.getBand(band).get(x, y);
          newValue = Math.abs(newValue - 255);
          output.getBand(band).set(x, y, newValue);
        }
      }
    }
  }

  /**
   * Applique le filtre médian
   * @param image
   * @param x coord x du pixel traité
   * @param y coord y du pixel traité
   * @param size taille du filtre
   * @return la valeur médiane des voisi du pixel au coordo (x,y)
   */
  private static int medianNeighbor(GrayU8 image, int x, int y, int size) {
    List<Integer> neighbors = new ArrayList<>();
    for (int i = x - size; i <= x + size; i++) {
      for (int j = y - size; j <= y + size; j++) {
        if (i >= 0 && i < image.width && j >= 0 && j < image.height) {
          neighbors.add(image.get(i, j));
        }
      }
    }
    Collections.sort(neighbors);
    int value;
    if (neighbors.size() % 2 == 0)
      value = (neighbors.get((neighbors.size() - 1) / 2) + neighbors.get((neighbors.size() - 1) / 2 + 1)) / 2;
    else
      value = (neighbors.get(neighbors.size() / 2));
    return value;
  }


  /**
   * Supprime le bruit d'une image
   * @param input
   * @param output
   * @param size taille du filtre
   */
  public static void removeNoise(Planar<GrayU8> input, Planar<GrayU8> output, int size) {
    for (int y = 0; y < input.height; ++y) {
      for (int x = 0; x < input.width; ++x) {
        for (int band = 0; band < input.getNumBands(); band++) {
          int newValue = medianNeighbor(input.getBand(band), x, y, size);
          output.getBand(band).set(x, y, newValue);
        }
      }
    }
  }


  /**
   * Inverse l'image horizontalement
   * @param input
   * @param output
   */
  private static void invertImageX(Planar<GrayU8> input, Planar<GrayU8> output) {
    for (int y = 0; y < input.height; ++y) {
      for (int x = 0; x < input.width; ++x) {
        for (int band = 0; band < input.getNumBands(); band++) {
          int value = input.getBand(band).get(x, y);
          output.getBand(band).set(input.getWidth() - x - 1, y, value);
        }
      }
    }
  }

  public static void invertImage(Planar<GrayU8> input, Planar<GrayU8> output, int X){
    if(X == 1){
      invertImageX(input, output);
    }
    else{
      invertImageY(input, output);
    }
  }

  /**
   * Inverse l'image verticalement
   * @param input
   * @param output
   */
  private static void invertImageY(Planar<GrayU8> input, Planar<GrayU8> output) {
    for (int y = 0; y < input.height; ++y) {
      for (int x = 0; x < input.width; ++x) {
        for (int band = 0; band < input.getNumBands(); band++) {
          int value = input.getBand(band).get(x, y);
          output.getBand(band).set(x, input.getHeight() - y - 1, value);
        }
      }
    }
  }

  /**
   * Effectue une rotation sur l'image
   * @param imageToRotate
   * @param imageOutput
   * @param thetaDegree angle de l'image en degré
   */
  public static void rotateImage(Planar<GrayU8> imageToRotate, Planar<GrayU8> imageOutput, int thetaDegree) {
    int w = imageToRotate.getWidth();
    int h = imageToRotate.getHeight();
    // conversion en radian
    double thetaRadians = Math.toRadians(thetaDegree);
    double centerX = w / 2.0;
    double centerY = h / 2.0;
    for (int x = 0; x < w; x++) {
      for (int y = 0; y < h; y++) {
        double oldX = (x - centerX) * Math.cos(thetaRadians) + (y - centerY) * Math.sin(thetaRadians) + centerX;
        double oldY = -(x - centerX) * Math.sin(thetaRadians) + (y - centerY) * Math.cos(thetaRadians) + centerY;
        for (int band = 0; band < imageToRotate.getNumBands(); band++) {
          if (oldX >= 0 && oldX < w && oldY >= 0 && oldY < h) {
            int pixelValue = imageToRotate.getBand(band).get((int) oldX, (int) oldY);
            imageOutput.getBand(band).set(x, y, pixelValue);
          }
        }
      }
    }
  }


  /**
   * Applique le filtre sepia sur l'image
   * @param input
   * @param output
   */
  public static void sepiaFilter(Planar<GrayU8> input, Planar<GrayU8> output) {

    for (int y = 0; y < input.height; y++) {
      for (int x = 0; x < input.width; x++) {
        int red = input.getBand(0).get(x, y);
        int green = input.getBand(1).get(x, y);
        int blue = input.getBand(2).get(x, y);

        int newRed = (int) (0.393 * red + 0.769 * green + 0.189 * blue);
        int newGreen = (int) (0.349 * red + 0.686 * green + 0.168 * blue);
        int newBlue = (int) (0.272 * red + 0.534 * green + 0.131 * blue);

        newRed = Math.min(newRed, 255);
        newGreen = Math.min(newGreen, 255);
        newBlue = Math.min(newBlue, 255);

        output.getBand(0).set(x, y, newRed);
        output.getBand(1).set(x, y, newGreen);
        output.getBand(2).set(x, y, newBlue);
        
      }
    }
  }

  /**
   * Conserve une seule couleur sur l'image, le reste est converti en niveau de gris
   * @param input
   * @param output
   * @param hueAngle angle correspondant à la couleur voulue
   * @param tolerance tolérance par rapport a la couleur choisie
   */
  public static void selectOneColorFilter(Planar<GrayU8> input, Planar<GrayU8> output, int hueAngle, int tolerance) {
    // float hueAngle = (float) Math.toRadians(hueAngleDegree);
    // Conversion de l'image en espace de couleur HSV
    Planar<GrayU8> hsvImage = input.clone();
    float[] hsv = new float[3];
    for (int y = 0; y < input.height; y++) {
      for (int x = 0; x < input.width; x++) {
        int r = input.getBand(0).get(x, y);
        int g = input.getBand(1).get(x, y);
        int b = input.getBand(2).get(x, y);
        ColorHsv.rgbToHsv(r, g, b, hsv);
        for (int band = 0; band < input.getNumBands(); band++) {
          double value = hsv[band];
          if (band == 0) {
            value = Math.toDegrees(value);
          }
          hsvImage.getBand(band).set(x, y, (int) value);
        }
      }
    }

    // Parcourir tous les pixels de l'image
    for (int y = 0; y < input.height; y++) {
      for (int x = 0; x < input.width; x++) {

        // Récupérer la valeur de l'angle de la couleur du pixel
        float hue = hsvImage.getBand(0).get(x, y);
        // Calculer la différence entre l'angle de la couleur du pixel et l'angle de la
        // couleur souhaitée
        float diff = Math.abs(hue - hueAngle);
        // S'assurer que la différence est dans l'intervalle de tolérance
        if (diff > 180) {
          diff = 360 - diff;
        }

        if (diff <= tolerance) {
          output.getBand(0).set(x, y, input.getBand(0).get(x, y));
          output.getBand(1).set(x, y, input.getBand(1).get(x, y));
          output.getBand(2).set(x, y, input.getBand(2).get(x, y));
        } else {
          // Convertir en noir et blanc
          int ng = (int) Math.round(input.getBand(0).get(x, y) * 0.3
              + input.getBand(1).get(x, y) * 0.59
              + input.getBand(2).get(x, y) * 0.11);
          output.getBand(0).set(x, y, ng);
          output.getBand(1).set(x, y, ng);
          output.getBand(2).set(x, y, ng);
        }
      }
    }
  }
}
