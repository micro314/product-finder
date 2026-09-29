import { useEffect, useState } from "react";
import { getIndexStatus } from "../services/api";
import type { IndexStatus } from "../types/index";

export function useIndexStatus() {
  const [status, setStatus] = useState<IndexStatus | null>(null);

  useEffect(() => {
    const loadStatus = () =>
      getIndexStatus()
        .then(setStatus)
        .catch(() => setStatus(null));
    void loadStatus();
    const refreshTimer = window.setInterval(() => {
      void loadStatus();
    }, 60_000);
    return () => window.clearInterval(refreshTimer);
  }, []);

  return status;
}
