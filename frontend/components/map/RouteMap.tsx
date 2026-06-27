"use client";
import { useEffect, useRef } from "react";
import "leaflet/dist/leaflet.css";

interface Props {
  origin: [number, number];
  destination: [number, number];
}

export default function RouteMap({ origin, destination }: Props) {
  const ref = useRef<HTMLDivElement>(null);
  const mapRef = useRef<import("leaflet").Map | null>(null);

  useEffect(() => {
    if (!ref.current || mapRef.current) return;

    import("leaflet").then((L) => {
      const center: [number, number] = [
        (origin[0] + destination[0]) / 2,
        (origin[1] + destination[1]) / 2,
      ];
      const map = L.map(ref.current!, { center, zoom: 14, zoomControl: false });

      L.tileLayer("https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png", {
        attribution: '© OpenStreetMap © CartoDB',
        subdomains: "abcd",
        maxZoom: 19,
      }).addTo(map);

      L.control.zoom({ position: "bottomright" }).addTo(map);

      // Route polyline (mocked straight line)
      L.polyline([origin, destination], {
        color: "#4ade80",
        weight: 4,
        opacity: 0.85,
        dashArray: "8 6",
      }).addTo(map);

      // Origin pin (blue)
      const originIcon = L.divIcon({
        className: "",
        html: `<div style="width:14px;height:14px;background:#38bdf8;border-radius:50%;border:3px solid #fff;box-shadow:0 0 0 3px rgba(56,189,248,0.4)"></div>`,
        iconSize: [14, 14],
        iconAnchor: [7, 7],
      });
      L.marker(origin, { icon: originIcon }).addTo(map);

      // Destination pin (green pulsing)
      const destIcon = L.divIcon({
        className: "",
        html: `<div style="position:relative;width:36px;height:36px">
          <div style="position:absolute;inset:0;background:rgba(74,222,128,0.25);border-radius:50%;animation:ping 1.5s cubic-bezier(0,0,0.2,1) infinite"></div>
          <div style="position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);width:16px;height:16px;background:#4ade80;border-radius:50%;border:2px solid #fff"></div>
        </div>
        <style>@keyframes ping{0%{transform:scale(1);opacity:.7}100%{transform:scale(2);opacity:0}}</style>`,
        iconSize: [36, 36],
        iconAnchor: [18, 18],
      });
      L.marker(destination, { icon: destIcon }).addTo(map);

      mapRef.current = map;
    });

    return () => {
      mapRef.current?.remove();
      mapRef.current = null;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return <div ref={ref} style={{ width: "100%", height: "100%" }} />;
}
