import { useState } from 'react'
import './App.css'
import { Footer } from './components/Footer'
import { Header } from './components/Header'
 import { IndexStatus } from './components/IndexStatus'
import { ResultsPanel } from './components/ResultsPanel'
import { SearchHero } from './components/SearchHero'
import { SearchHistory } from './components/SearchHistory'
import { useAuth } from './hooks/useAuth'
import { useProductSearch } from './hooks/useProductSearch'
import { useIndexStatus } from './hooks/useIndexStatus'

function App() {
  const { token, user, authMessage, signIn, register, signOut } = useAuth()
  const { results, failures, history, loading, error, search, removeHistoryItem, clearHistory } = useProductSearch(token)
  const indexStatus = useIndexStatus()
  const [query, setQuery] = useState('RTX 4070')
  const [limit, setLimit] = useState(20)

  return <div className="app-shell"><Header user={user} onSignIn={() => void signIn()} onRegister={() => void register()} onSignOut={signOut} /><main><SearchHero query={query} limit={limit} loading={loading} authenticated={Boolean(token)} authMessage={authMessage} onQueryChange={setQuery} onLimitChange={setLimit} onSearch={() => void search(query, limit)} /><IndexStatus status={indexStatus} /><section className="content-grid"><SearchHistory history={history} onSelect={(selectedQuery) => { setQuery(selectedQuery); void search(selectedQuery, limit) }} onDelete={(id) => void removeHistoryItem(id)} onClear={() => void clearHistory()} /><ResultsPanel results={results} failures={failures} error={error} loading={loading} authenticated={Boolean(token)} /></section></main><Footer /></div>
}

export default App
