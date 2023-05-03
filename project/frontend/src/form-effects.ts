/**
 * Interfaces servant à générer le formulaire d'un effet
 */

//les attributs htmls
interface HtmlAtt {
    value?: string | number;
    param?: any;
    text?: string;
    min?: number;
    max?: number;
    name?: string;

}

//un input du formulaire
interface Line {
    htmlAtt: HtmlAtt;
    type: string;
    options?: HtmlAtt[];
}

//un formulaire d'effet
export interface FormEffects {
    htmlAtt: HtmlAtt

    lines?: Line[];
}

function upFirstLetter(string: string): string {
    return string.charAt(0).toUpperCase() + string.slice(1)
}

export function getText(att: HtmlAtt): string {
    return att.text != undefined ? att.text : (att.name != undefined ? upFirstLetter(att.name) : '');
}

//Tableau contenant toutes les informations de chaque formulaire d'effet
export const allFormEffects: FormEffects[] = [{
    htmlAtt: {
        text: "Luminosity",
        name: "brightnessChanger",

    },

    lines: [{
        htmlAtt: {
            text: "Gain",
            value: 0,
            min: -255,
            max: 255,
            
        },
        type: "range"
    }
    ]
}, {
    htmlAtt: {
        text: "Color image",
        name: "colorImage",
    },
    lines: [{
        htmlAtt: {
            text: "Hue",
            value: 0,
            min: 0,
            max: 365,
          
        },
        type: "range"
    }]
}, {
    htmlAtt: {
        text: "Convert to gray",
        name: "convertColorToGray"
    }
}, {
    htmlAtt: {
        text: "Show edges",
        name: "showContours"
    }
},
{
    htmlAtt: {
        text: "Histogram equalization",
        name: "histogramEqualization",
    },

},
{
    htmlAtt: {

        text: "Blur filter",
        name: "blurImage",
    },

    lines: [{
        type: "select",
        htmlAtt: {

            text: "Mode"
        },
        options: [{
            name: "0",
            text: "Skip"
        }, {
            name: "1",
            text: "Normalized"
        }, {
            name: "2",
            text: "Extended"
        }, {
            name: "3",
            text: "Reflect"
        }]

    }, {
        type: "number",
        htmlAtt: {
            name: "size",
            text: "Blur's level",
            value: 0,
            min: 0,
            max: 200
        }


    }
    ]
},
{
    htmlAtt: {
        text: "Convert to negative",
        name: "negativeFilter"
    }
}, {
    htmlAtt: {
        text: "Remove noise",
        name: "removeNoise"
    },
    lines: [{
        htmlAtt: {
            text: "size",
            value: 1,
            min: 1,
            max: 50,
            
        },
        type: "range"
    }

    ]
}, {
    htmlAtt: {
        text: "Invert",
        name: "invertImage",
    },

    lines: [{
        type: "select",
        htmlAtt: {

            text: "Axis"
        },
        options: [{
            name: "0",
            text: "Y"
        }, {
            name: "1",
            text: "X"
        }]

    }
    ]
}, {
    htmlAtt: {
        text: "Rotate Image",
        name: "rotateImage"
    },
    lines: [{
        htmlAtt: {
            text: "Angle in °",
            value: 0,
            min: -360,
            max: 360,
          
        },
        type: "range"
    }]

},
{
    htmlAtt: {
        text: "Sepia filter",
        name: "sepiaFilter"
    }
},
{
    htmlAtt:{
        text: "Isolate color",
        name: "selectOneColorFilter"
    },lines: [{
        htmlAtt: {
            text: "Couleur",
            value: 0,
            min: 0,
            max: 360,
            
        },
        type: "range"
    },{
        htmlAtt:{
            text: "tolerance",
            value: 10,
            min: 1,
            max: 100,
        },
        type: "range"
    }

    ]
}]