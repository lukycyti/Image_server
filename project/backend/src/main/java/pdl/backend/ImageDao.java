package pdl.backend;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class ImageDao implements Dao<Image> {

  private final Map<Long, Image> images = new HashMap<>();

  public ImageDao() {
    File imageFile = new File("images");
    List<File> imageFiles = getImages(imageFile);
    imageFiles.forEach(iF -> {
      byte[] byteArray;
      try {
        byteArray = Files.readAllBytes(Paths.get(iF.getAbsolutePath()));
        Image image = new Image(iF.getName(), byteArray);
        this.create(image);
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }

  private List<File> getImages(File folder){
    List<File> imageList = new ArrayList<File>();
    if (folder.isDirectory()) {
      File[] fileList = folder.listFiles();
      for (File file : fileList) {
        if (file.isDirectory()) {
          imageList.addAll(getImages(file));
        } else if (file.isFile() && isImage(file)) {
          imageList.add(file);
        }
      }
    } else if (folder.isFile() && isImage(folder)) {
      imageList.add(folder);
    }
    return imageList;
  }

  private static boolean isImage(File file) {
    String name = file.getName().toLowerCase();
    return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
  }

  @Override
  public Optional<Image> retrieve(final long id) {
    return Optional.ofNullable(images.get(id));
  }

  @Override
  public List<Image> retrieveAll() {
    return new ArrayList<Image>(images.values());
  }

  @Override
  public void create(final Image img) {
    images.put(img.getId(), img);
  }

  @Override
  public void update(final Image img, final String[] params) {
    img.setName(Objects.requireNonNull(params[0], "Name cannot be null"));

    images.put(img.getId(), img);
  }

  @Override
  public void delete(final Image img) {
    images.remove(img.getId());
  }
}
