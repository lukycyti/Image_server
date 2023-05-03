export function modulo(x: number,y: number): number {
    return ((x % y) + y) % y;
}