// Backend API location.
// - Opened from your own PC (localhost / 127.0.0.1): use the local Spring Boot backend.
// - Opened from the deployed site (Vercel): use the backend running in GitHub Codespaces.
//   (If you create a new codespace, put its port-8080 address here.
//    Render alternative: https://sport-management-system-api.onrender.com/api)
const LOCAL_API = "http://localhost:8080/api";
const PRODUCTION_API = "https://zany-fishstick-wv4p6p9wvw5qhwq9-8080.app.github.dev/api";

const isLocal = ["localhost", "127.0.0.1"].includes(window.location.hostname);

export const API_BASE_URL = isLocal ? LOCAL_API : PRODUCTION_API;
