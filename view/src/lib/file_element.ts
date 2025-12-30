export type FileElement = {
  fileType: FileType;
  name: string;
  imageId: number | null;
};

export const FileType = {
  File: 'File',
  Directory: 'Directory'
} as const;
export type FileType = (typeof FileType)[keyof typeof FileType];
