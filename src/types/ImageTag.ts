import type { Person } from './Person';

export type ImageTagType = {
  id: string;
  x: number;
  y: number;
  name?: string;
  user?: Person;
  isTemp?: boolean;
};
