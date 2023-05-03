package pdl.backend;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
  
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import boofcv.io.image.ConvertBufferedImage;

import boofcv.struct.image.GrayU8;

import boofcv.struct.image.Planar;

@RestController
public class ImageController {

  @Autowired
  private ObjectMapper mapper;

  private final ImageDao imageDao;

  final String serverURL = "http://localhost:8080/";

  @Autowired
  public ImageController(ImageDao imageDao) {
    this.imageDao = imageDao;
  }

  
  @RequestMapping(value = "/images/{id}", method = RequestMethod.GET, produces = { MediaType.IMAGE_JPEG_VALUE,
      MediaType.IMAGE_PNG_VALUE })
  public ResponseEntity<?> getImage(@PathVariable("id") long id,
      @RequestParam(required = false) Map<String, String> param) throws ClassNotFoundException, NoSuchMethodException,
      SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

    Optional<Image> optionalImage = imageDao.retrieve(id);

    if (optionalImage.isPresent()) {
      return getImage(optionalImage, param);
    }
    return new ResponseEntity<>("Image id=" + id + " not found.", HttpStatus.NOT_FOUND);
  }

  @RequestMapping(value = "/images/fromdata", method = RequestMethod.POST, produces = { MediaType.IMAGE_JPEG_VALUE,
      MediaType.IMAGE_PNG_VALUE })
  public ResponseEntity<?> getImage(@RequestParam("file") MultipartFile file, @RequestParam(required = false) Map<String, String> param) throws ClassNotFoundException, NoSuchMethodException,
      SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

    String contentType = file.getContentType();
   
    System.out.println(contentType + "  " + MediaType.IMAGE_JPEG.toString());
    if (!contentType.equals(MediaType.IMAGE_JPEG.toString()) && !contentType.equals("image/jpg")
        && !contentType.equals(MediaType.IMAGE_PNG_VALUE.toString())) {
      return new ResponseEntity<>("Only JPEG and PNG file format supported\n", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }
    System.out.println(param.values());
 
    try {
  
      return getImage(Optional.ofNullable(new Image(file.getOriginalFilename(), file.getBytes())),param);
    } catch (IOException e) {
      return new ResponseEntity<>("Failure to read file\n", HttpStatus.NO_CONTENT);
    }
   
  }
  

  private ResponseEntity<?> getImage(Optional<Image> optionalImage, Map<String, String> param) {
    InputStream inputStream = new ByteArrayInputStream(optionalImage.get().getData());
    try {

      BufferedImage bufferedImage = ImageIO.read(inputStream);
      Planar<GrayU8> image = ConvertBufferedImage.convertFromPlanar(bufferedImage, null, true, GrayU8.class);
      ConvertBufferedImage.convertFrom(bufferedImage, image, true);
      Planar<GrayU8> output = image.createSameShape();
      if (param.containsKey("algorithm")) {
        Set<String> keys = param.keySet();
        String algorithm = param.get("algorithm");
        

        keys.remove("algorithm");
        int nbArg = keys.size();
        String keysString[] = new String[nbArg];
        int j = 0;
        for (String key : keys) {
          keysString[j] = key;
          j++;

        }
        Class<?>[] cArg = new Class[nbArg + 2];
        Object[] arg = new Object[nbArg + 2];
        arg[0] = image;
        arg[1] = output;
        for (int i = 0; i < nbArg + 2; i++) {
          if (i < 2) {
            cArg[i] = image.getClass();
          } else {
            try {
              int p = Integer.parseInt(param.get(keysString[i - 2]));
              arg[i] = p;
              cArg[i] = int.class;
            } catch (Exception e) {
              System.out.println(e.getMessage());
            }
          }
        }
        Method method = ImageProcessing.class.getDeclaredMethod(algorithm, cArg);
        method.invoke(ImageProcessing.class, arg);
      } else
        output = image;
      BufferedImage outputBufferedImage = ConvertBufferedImage.convertTo(output, null, true);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(outputBufferedImage, "jpg", baos);
      byte[] imageBytes = baos.toByteArray();
      return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG)
          .body(new InputStreamResource(new ByteArrayInputStream(imageBytes)));

    } catch (Exception e) {
      if (!(e instanceof NoSuchMethodException || e instanceof InvocationTargetException)) {
        e.printStackTrace();
        return new ResponseEntity<>("Image not found.", HttpStatus.INTERNAL_SERVER_ERROR);
      }
      String str = "méthode inexistante ou erreur de paramètres";
      if (e instanceof InvocationTargetException)
        str = e.getCause().getMessage();

      System.err.println(str);
      return new ResponseEntity<>("Image not found.", HttpStatus.BAD_REQUEST);
    }
  }

  @RequestMapping(value = "/images/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<?> deleteImage(@PathVariable("id") long id) {

    Optional<Image> image = imageDao.retrieve(id);

    if (image.isPresent()) {
      imageDao.delete(image.get());
      return new ResponseEntity<>("Image id=" + id + " deleted.", HttpStatus.OK);
    }
    return new ResponseEntity<>("Image id=" + id + " not found.", HttpStatus.NOT_FOUND);
  }

  @RequestMapping(value = "/images", method = RequestMethod.POST)
  public ResponseEntity<?> addImage(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {

    String contentType = file.getContentType();
    if (!contentType.equals(MediaType.IMAGE_JPEG.toString())
        && !contentType.equals(MediaType.IMAGE_PNG_VALUE.toString()) && !contentType.equals("image/jpg")) {
      return new ResponseEntity<>("Only JPEG file format supported\n", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    try {
      imageDao.create(new Image(file.getOriginalFilename(), file.getBytes()));
    } catch (IOException e) {
      return new ResponseEntity<>("Failure to read file\n", HttpStatus.NO_CONTENT);
    }
    return new ResponseEntity<>("Image uploaded\n", HttpStatus.OK);
  }

  @RequestMapping(value = "/images", method = RequestMethod.GET, produces = "application/json")
  @ResponseBody
  public ArrayNode getImageList() {
    List<Image> images = imageDao.retrieveAll();
    ArrayNode nodes = mapper.createArrayNode();
    int numImage = 0;
    for (Image image : images) {
      ObjectNode objectNode = mapper.createObjectNode();
      objectNode.put("id", image.getId());
      objectNode.put("name", image.getName());
      String ext = FilenameUtils.getExtension(image.getName());
      objectNode.put("type", ext);
      try {
        URL imageUrl = new URL(serverURL + "images/" + numImage);
        BufferedImage bufImage = ImageIO.read(imageUrl);
        int width = bufImage.getWidth();
        int height = bufImage.getHeight();
        objectNode.put("size", width + "x" + height);
      } catch (Exception e) {
        System.err.println("Lecture de l'image à partir de cet URL impossible");
      }
      numImage++;
      nodes.add(objectNode);
    }
    return nodes;
  }

}
