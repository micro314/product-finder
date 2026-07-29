type SearchHistoryProps = { history: string[]; onSelect: (query: string) => void }

export function SearchHistory({ history, onSelect }: SearchHistoryProps) {
  return <aside className="sidebar"><div className="section-label">RECENT SEARCHES</div>{history.length ? history.map((item) => <button className="history-item" key={item} onClick={() => onSelect(item)}><span>↗</span>{item}</button>) : <p className="muted">Your searches will appear here.</p>}<div className="tip"><span className="tip-icon">✦</span><strong>Search smarter</strong><p>Use a model name, chipset, or memory size to get the most relevant matches.</p></div></aside>
}
