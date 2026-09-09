export type ToolCategory = 
  | 'all'
  | 'core'
  | 'convert'
  | 'adjust'
  | 'creative'
  | 'utilities';

export interface ImageToolDefinition {
  id: string;
  name: string;
  category: ToolCategory;
  description: string;
  iconName: string;
  badge?: string;
}

export interface ExifTag {
  name: string;
  value: string;
}

export interface ColorSwatch {
  hex: string;
  rgb: string;
  hsl: string;
  population?: number;
}
