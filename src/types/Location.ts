export interface LocationResult {
  title: string;
  subtitle: string;
  type: 'city' | 'district' | 'attraction' | 'park' | 'current';
  lat: string | null;
  lon: string | null;
  searchbox_id?: string;
}