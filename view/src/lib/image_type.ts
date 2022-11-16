import _ from 'lodash';

export type Geom = {
	id: number;
	address: string;
	lat: number;
	lon: number;
};

export type ImageFile = {
	id: number;
	path: string;
	filesize: number;
};

export type Exif = {
	camera: string;
	lens: string | null;
	focal: number | null;
	aperture: number | null;
	exposureTime: string;
	iso: number;
}

export type ImageData = {
	id: number;
	cameraId: number;
	shotId: number;
	shootingAt: string;
	geo: Geom | null;
	files: ImageFile[];
	exif: Exif | null;
};

export type Tag = {
	id: number;
	name: string;
}

export function thumbnail(image: ImageData): ImageFile {
	const minImage = _.minBy(image.files, (f) => f.filesize);
	if (minImage) return minImage;
	throw new Error('image.files is empty');
}
