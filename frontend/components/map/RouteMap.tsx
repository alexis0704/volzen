"use client";
import { useEffect, useRef } from "react";
import "leaflet/dist/leaflet.css";

interface Props {
  origin: [number, number];
  destination: [number, number];
  onRouteInfo?: (info: { distanceKm: number; durationMinutes: number; source: "route" | "fallback" }) => void;
}

type LeafletContainer = HTMLDivElement & {
  _leaflet_id?: number;
};

function resetLeafletContainer(container: LeafletContainer) {
  container.replaceChildren();
  delete container.dataset.leafletInitialized;
  delete container._leaflet_id;
}

export default function RouteMap({ origin, destination, onRouteInfo }: Props) {
  const ref = useRef<LeafletContainer>(null);
  const mapRef = useRef<import("leaflet").Map | null>(null);

  useEffect(() => {
    const container = ref.current;
    let disposed = false;

    if (!container || mapRef.current) return;

    import("leaflet").then((L) => {
      if (disposed || mapRef.current) return;

      if (container.dataset.leafletInitialized === "true" || container._leaflet_id) {
        resetLeafletContainer(container);
      }

      const center: [number, number] = [
        (origin[0] + destination[0]) / 2,
        (origin[1] + destination[1]) / 2,
      ];
      const map = L.map(container, { center, zoom: 14, zoomControl: false });
      container.dataset.leafletInitialized = "true";

      if (disposed) {
        map.remove();
        resetLeafletContainer(container);
        return;
      }

      L.tileLayer("https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png", {
        attribution: '© OpenStreetMap © CartoDB',
        subdomains: "abcd",
        maxZoom: 19,
      }).addTo(map);

      L.control.zoom({ position: "bottomright" }).addTo(map);

      const drawFallbackRoute = () => {
        const fallbackLine = L.polyline([origin, destination], {
          color: "#4ade80",
          weight: 4,
          opacity: 0.85,
          dashArray: "8 6",
        }).addTo(map);
        map.fitBounds(fallbackLine.getBounds(), { padding: [48, 48] });
        onRouteInfo?.({
          distanceKm: haversineKm(origin, destination),
          durationMinutes: Math.max(1, Math.round((haversineKm(origin, destination) / 25) * 60)),
          source: "fallback",
        });
      };

      fetchRoute(origin, destination)
        .then((route) => {
          if (disposed) return;
          if (!route) {
            drawFallbackRoute();
            return;
          }

          const line = L.polyline(route.coordinates, {
            color: "#4ade80",
            weight: 5,
            opacity: 0.9,
          }).addTo(map);
          map.fitBounds(line.getBounds(), { padding: [48, 48] });
          onRouteInfo?.({
            distanceKm: route.distanceKm,
            durationMinutes: route.durationMinutes,
            source: "route",
          });
        })
        .catch(() => {
          if (!disposed) drawFallbackRoute();
        });

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
      disposed = true;
      mapRef.current?.remove();
      mapRef.current = null;
      resetLeafletContainer(container);
    };
  }, [origin, destination, onRouteInfo]);

  return <div ref={ref} style={{ width: "100%", height: "100%" }} />;
}

async function fetchRoute(origin: [number, number], destination: [number, number]) {
  const url = new URL(`https://router.project-osrm.org/route/v1/driving/${origin[1]},${origin[0]};${destination[1]},${destination[0]}`);
  url.searchParams.set("overview", "full");
  url.searchParams.set("geometries", "geojson");
  url.searchParams.set("steps", "false");

  const response = await fetch(url);
  if (!response.ok) return null;

  const data = await response.json() as {
    routes?: Array<{
      distance: number;
      duration: number;
      geometry: {
        coordinates: Array<[number, number]>;
      };
    }>;
  };
  const route = data.routes?.[0];
  if (!route) return null;

  return {
    coordinates: route.geometry.coordinates.map(([lng, lat]) => [lat, lng] as [number, number]),
    distanceKm: route.distance / 1000,
    durationMinutes: Math.max(1, Math.round(route.duration / 60)),
  };
}

function haversineKm(origin: [number, number], destination: [number, number]) {
  const earthRadiusKm = 6371;
  const dLat = toRadians(destination[0] - origin[0]);
  const dLng = toRadians(destination[1] - origin[1]);
  const lat1 = toRadians(origin[0]);
  const lat2 = toRadians(destination[0]);
  const a = Math.sin(dLat / 2) ** 2 + Math.sin(dLng / 2) ** 2 * Math.cos(lat1) * Math.cos(lat2);
  return earthRadiusKm * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

function toRadians(value: number) {
  return (value * Math.PI) / 180;
}
