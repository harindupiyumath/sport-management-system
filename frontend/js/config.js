// Backend API location.
// - Opened from your own PC (localhost / 127.0.0.1): use the local Spring Boot backend.
// - Opened from the deployed site (Vercel): use the hosted backend on Render.
const LOCAL_API = "http://localhost:8080/api";
const PRODUCTION_API = "https://sport-management-system-api.onrender.com/api";

const isLocal = ["localhost", "127.0.0.1"].includes(window.location.hostname);

export const API_BASE_URL = isLocal ? LOCAL_API : PRODUCTION_API;
