import type { IndexStatus as IndexStatusData } from '../types/index'

function relativeTime(timestamp: string | null) {
  if (!timestamp) return 'Not polled yet'
  const elapsedMinutes = Math.max(0, Math.floor((Date.now() - Date.parse(timestamp)) / 60_000))
  if (elapsedMinutes < 1) return 'Just now'
  if (elapsedMinutes < 60) return `${elapsedMinutes} minute${elapsedMinutes === 1 ? '' : 's'} ago`
  const elapsedHours = Math.floor(elapsedMinutes / 60)
  if (elapsedHours < 24) return `${elapsedHours} hour${elapsedHours === 1 ? '' : 's'} ago`
  const elapsedDays = Math.floor(elapsedHours / 24)
  return `${elapsedDays} day${elapsedDays === 1 ? '' : 's'} ago`
}

export function IndexStatus({ status }: { status: IndexStatusData | null }) {
  if (!status) return null

  return <div className="index-status" aria-label="Index status">
    <div><strong>{status.vendorCount}</strong><span>VENDORS INDEXED</span></div>
    <div><strong>{status.recordCount.toLocaleString()}</strong><span>CARDS IN INDEX</span></div>
    <div><strong>{relativeTime(status.lastPolledAt)}</strong><span>LAST REMOTE POLL</span></div>
  </div>
}
