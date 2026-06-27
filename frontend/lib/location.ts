export type Coordinates = {
  lat: number;
  lng: number;
};

export const DEFAULT_LOCATION: Coordinates = {
  lat: 10.775,
  lng: 106.698,
};

export function getCurrentLocation(options?: PositionOptions): Promise<Coordinates> {
  if (typeof navigator === "undefined" || !navigator.geolocation) {
    return Promise.reject(new Error("Geolocation is not available."));
  }

  return new Promise((resolve, reject) => {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        resolve({
          lat: position.coords.latitude,
          lng: position.coords.longitude,
        });
      },
      reject,
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 60000,
        ...options,
      },
    );
  });
}

export function watchCurrentLocation(
  onLocation: (location: Coordinates) => void,
  onError?: (error: GeolocationPositionError) => void,
) {
  if (typeof navigator === "undefined" || !navigator.geolocation) {
    return null;
  }

  return navigator.geolocation.watchPosition(
    (position) => {
      onLocation({
        lat: position.coords.latitude,
        lng: position.coords.longitude,
      });
    },
    onError,
    {
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 15000,
    },
  );
}
