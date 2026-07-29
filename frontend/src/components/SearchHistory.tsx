import type { HistoryItem } from '../types/history'

type SearchHistoryProps = { history: HistoryItem[]; onSelect: (query: string) => void; onDelete: (id: number) => void; onClear: () => void }

export function SearchHistory({ history, onSelect, onDelete, onClear }: SearchHistoryProps) {
  return <aside className="sidebar"><div className="history-heading"><div className="section-label">RECENT SEARCHES</div>{history.length > 0 && <button className="clear-history" onClick={onClear}>Clear all</button>}</div>{history.length ? history.map((item) => <div className="history-row" key={item.id}><button className="history-item" onClick={() => onSelect(item.query)}><span>↗</span>{item.query}</button><button className="delete-history" aria-label={`Delete search ${item.query}`} onClick={() => onDelete(item.id)}>×</button></div>) : <p className="muted">Your searches will appear here.</p>}<div className="tip"><span className="tip-icon">✦</span><strong>Search smarter</strong><p>Use a model name, chipset, or memory size to get the most relevant matches.</p></div></aside>
}
