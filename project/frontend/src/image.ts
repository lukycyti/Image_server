/**
 * Interface gérant une image
 */
export interface ImageType {
  id: number;
  name: string;
  size: string;
  type: string;
  origin: string;
  memory: string[];
  index: number;
}

/*
* Initialise une image
*/
export function initialize(image: ImageType, href:string){
  image.index = 0;
  image.memory = [];
  image.origin = href;
  setImage(image,href);
}

/*
* Change la position de l'index. L'index correspond à l'indice du tableau de mémoire d'images correspondant à l'image affichée à l'utilisateur
*/
export function switchImage(image: ImageType, delta: number){
  var newIndex = image.index + delta;
  if(newIndex >= 0 && newIndex < image.memory.length){
    image.index = newIndex;
  }
}

/*
* Réinitialise l'image
*/
export function reset(image: ImageType){
  initialize(image, image.origin);
}

/*
* Retourne l'image en chaine de caractères
*/
export function getSrc(image: ImageType){
  return image.memory[image.index];
}
export function canGoFrwd(image: ImageType): boolean{
  return image.index !== image.memory.length -1;
}

export function canGoBack(image: ImageType): boolean{
  console.log(image.index);
  return image.index > 0;
}

/**
 * Ajoute au tableau de mémoire une image en chaine de caractère, et supprime le premier élément de celui-ci si il a une taille supérieure ou égale à 10.
 * @param image 
 * @param src 
 */
export function setImage(image: ImageType, src: string){
  var length = image.memory.length
  if(length >= 10){
    while(image.memory.length !== 9){
      image.memory.shift();
    }
  }
  if(image.index !== 0 && length - 1 !== image.index){
    image.memory.length = image.index + 1;
  }
  
  image.memory.push(src);
  image.index = image.memory.length - 1;
}