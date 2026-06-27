"use client";
import { useEffect, useRef } from "react";
import type { Provider } from "@/lib/mock-data";
import "leaflet/dist/leaflet.css";

interface Props {
  providers: Provider[];
  selected: string | null;
  onSelect: (id: string) => void;
}

export default function LeafletMap({ providers, selected, onSelect }: Props) {
  const containerRef = useRef<HTMLDivElement>(null);
  const mapRef = useRef<import("leaflet").Map | null>(null);
  const markersRef = useRef<Map<string, import("leaflet").Marker>>(new Map());

  useEffect(() => {
    if (!containerRef.current || mapRef.current) return;

    import("leaflet").then((L) => {
      // Fix default icon path (webpack mangles it)
      // ponytail: custom green marker via DivIcon instead of default png
      const map = L.map(containerRef.current!, {
        center: [10.775, 106.698],
        zoom: 14,
        zoomControl: false,
      });

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
        markersRef.current.set(p.id, marker);
      });

      mapRef.current = map;
    });

    return () => {
      mapRef.current?.remove();
      mapRef.current = null;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Pan to selected
  useEffect(() => {
    if (!selected || !mapRef.current) return;
    const p = providers.find((pr) => pr.id === selected);
    if (p) mapRef.current.flyTo([p.lat, p.lng], 15, { duration: 0.8 });
  }, [selected, providers]);

  return <div ref={containerRef} style={{ width: "100%", height: "100%" }} />;
}
