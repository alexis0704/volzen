"use client";
import { useEffect, useRef, useState } from "react";
import type { Provider } from "@/lib/domain";
import { DEFAULT_LOCATION } from "@/lib/location";
import type { Coordinates } from "@/lib/location";
import "leaflet/dist/leaflet.css";

interface Props {
  providers: Provider[];
  selected: string | null;
  onSelect: (id: string) => void;
  currentLocation?: Coordinates;
}

type LeafletContainer = HTMLDivElement & {
  _leaflet_id?: number;
};

function resetLeafletContainer(container: LeafletContainer) {
  container.replaceChildren();
  delete container.dataset.leafletInitialized;
  delete container._leaflet_id;
}

export default function LeafletMap({ providers, selected, onSelect, currentLocation = DEFAULT_LOCATION }: Props) {
  const containerRef = useRef<LeafletContainer>(null);
  const mapRef = useRef<import("leaflet").Map | null>(null);
  const markersRef = useRef<Map<string, import("leaflet").Marker>>(new Map());
  const currentMarkerRef = useRef<import("leaflet").Marker | null>(null);
  const [mapReady, setMapReady] = useState(false);

  useEffect(() => {
    const container = containerRef.current;
    const markers = markersRef.current;
    let disposed = false;

    if (!container || mapRef.current) return;

    import("leaflet").then((L) => {
      if (disposed || mapRef.current) return;

      if (container.dataset.leafletInitialized === "true" || container._leaflet_id) {
        resetLeafletContainer(container);
      }

      // Fix default icon path (webpack mangles it)
      // ponytail: custom green marker via DivIcon instead of default png
      const map = L.map(container, {
        center: [currentLocation.lat, currentLocation.lng],
        zoom: 14,
        zoomControl: false,
      });
      container.dataset.leafletInitialized = "true";

      if (disposed) {
        map.remove();
        resetLeafletContainer(container);
        return;
      }

      const isDark = document.documentElement.dataset.theme !== "light";
      const tileUrl = isDark
        ? "https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
        : "https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png";
      L.tileLayer(tileUrl, {
        attribution: '© <a href="https://www.openstreetmap.org">OSM</a> © CartoDB',
        subdomains: "abcd",
        maxZoom: 19,
      }).addTo(map);

      L.control.zoom({ position: "bottomright" }).addTo(map);

      mapRef.current = map;
      setMapReady(true);
    });

    return () => {
      disposed = true;
      markers.clear();
      currentMarkerRef.current = null;
      mapRef.current?.remove();
      mapRef.current = null;
      setMapReady(false);
      resetLeafletContainer(container);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (!mapReady || !mapRef.current) return;

    let disposed = false;
    const markers = markersRef.current;
    const map = mapRef.current;

    import("leaflet").then((L) => {
      if (disposed) return;

      markers.forEach((marker) => marker.remove());
      markers.clear();

      providers.forEach((p) => {
        const icon = L.divIcon({
          className: "",
          html: `<div style="
            background: #4ade80;
            color: #0a0f0d;
            border-radius: 50% 50% 50% 0;
            width: 32px; height: 32px;
            display: flex; align-items: center; justify-content: center;
            font-weight: 700; font-size: 10px;
            transform: rotate(-45deg);
            box-shadow: 0 2px 8px rgba(74,222,128,0.5);
          ">
            <span style="transform: rotate(45deg)">${Math.round(p.pricePerHour / 1000)}k</span>
          </div>`,
          iconSize: [32, 32],
          iconAnchor: [16, 32],
        });
        const marker = L.marker([p.lat, p.lng], { icon })
          .addTo(map)
          .on("click", () => onSelect(p.id));
        markers.set(p.id, marker);
      });
    });

    return () => {
      disposed = true;
    };
  }, [providers, onSelect, mapReady]);

  useEffect(() => {
    if (!mapReady || !mapRef.current) return;

    let disposed = false;
    const map = mapRef.current;

    import("leaflet").then((L) => {
      if (disposed) return;

      const nextLatLng: [number, number] = [currentLocation.lat, currentLocation.lng];
      if (currentMarkerRef.current) {
        currentMarkerRef.current.setLatLng(nextLatLng);
      } else {
        const currentIcon = L.divIcon({
          className: "",
          html: `<div style="width:18px;height:18px;background:#38bdf8;border-radius:50%;border:3px solid #fff;box-shadow:0 0 0 5px rgba(56,189,248,0.25)"></div>`,
          iconSize: [18, 18],
          iconAnchor: [9, 9],
        });
        currentMarkerRef.current = L.marker(nextLatLng, { icon: currentIcon }).addTo(map);
      }

      if (!selected) {
        map.flyTo(nextLatLng, 14, { duration: 0.6 });
      }
    });

    return () => {
      disposed = true;
    };
  }, [currentLocation, selected, mapReady]);

  // Pan to selected
  useEffect(() => {
    if (!selected || !mapRef.current) return;
    const p = providers.find((pr) => pr.id === selected);
    if (p) mapRef.current.flyTo([p.lat, p.lng], 15, { duration: 0.8 });
  }, [selected, providers]);

  return <div ref={containerRef} style={{ width: "100%", height: "100%" }} />;
}
