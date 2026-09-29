import { useState } from "react";
import type { HistoryItem } from "../types/history";
import type { ProductFilters } from "../types/filters";

type SearchHistoryProps = {
  history: HistoryItem[];
  onSelect: (query: string, filters: ProductFilters) => void;
  onDelete: (id: number) => void;
  onClear: () => void;
};

export function SearchHistory({
  history,
  onSelect,
  onDelete,
  onClear,
}: SearchHistoryProps) {
  const [expanded, setExpanded] = useState(false);
  const visibleHistory = expanded ? history : history.slice(0, 5);

  return (
    <aside className="sidebar">
      <div className="history-heading">
        <div className="section-label">RECENT SEARCHES</div>
        {history.length > 0 && (
          <button className="clear-history" onClick={onClear}>
            Clear all
          </button>
        )}
      </div>
      {history.length ? (
        <>
          {visibleHistory.map((item) => (
            <div className="history-row" key={item.id}>
              <button
                className="history-item"
                onClick={() =>
                  onSelect(item.searchQuery ?? item.query, item.filters ?? {})
                }
              >
                <span>↗</span>
                {item.query}
              </button>
              <button
                className="delete-history"
                aria-label={`Delete search ${item.query}`}
                onClick={() => onDelete(item.id)}
              >
                ×
              </button>
            </div>
          ))}
          {history.length > 5 && (
            <button
              className="show-more"
              onClick={() => setExpanded((value) => !value)}
            >
              {expanded ? "Show less" : `Show more (${history.length - 5})`}
            </button>
          )}
        </>
      ) : (
        <p className="muted">Your searches will appear here.</p>
      )}
    </aside>
  );
}
