import { useState } from 'react'

function App() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [timeoutMs, setTimeoutMs] = useState(15000) // Default 15s

  const fetchData = async () => {
    setLoading(true)
    setError(null)
    setData(null)

    // ===========================================
    // CRITICALLY IMPORTANT: TIMEOUT HANDLING CODE
    // ===========================================

    // 1. Create an AbortController to be able to cancel the request
    const controller = new AbortController()
    const signal = controller.signal

    // 2. Set a timer that will trigger the abort signal after 'timeoutMs'
    // This prevents the UI from checking or waiting indefinitely.
    const timeoutId = setTimeout(() => {
      console.warn(`Fetch timed out after ${timeoutMs}ms`)
      controller.abort("Timeout")
    }, parseInt(timeoutMs))

    try {
      const startTime = performance.now()

      // 3. Pass the 'signal' to the fetch call
      // The browser will check this signal and abort the network request if fired.
      const response = await fetch('http://localhost:8080/fetch-remote-data', {
        signal: signal
      })

      // 4. IMPORTANT: Clear the timeout if the request completes successfully
      // If we don't clear it, it might fire later unnecessarily (though less harmful after completion).
      clearTimeout(timeoutId)

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      // Parse the large JSON
      const result = await response.json()
      const duration = (performance.now() - startTime).toFixed(2)

      setData({
        items: result,
        duration: duration,
        sizeBytes: new TextEncoder().encode(JSON.stringify(result)).length
      })

    } catch (e) {
      // 5. Handle the AbortError specifically to know it was a timeout
      if (e.name === 'AbortError') {
        setError(`Request timed out after ${timeoutMs}ms! The data is too large or the network is too slow.`)
      } else {
        setError(`Error: ${e.message}`)
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <h1>Enterprise Data Viewer</h1>
      <p style={{ opacity: 0.7 }}>Microservices Demo: React &rarr; Spring Boot Client &rarr; Spring Boot Data Service</p>

      <div className="card">
        <div style={{ marginBottom: '20px', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '10px' }}>
          <label>
            Timeout (ms):
            <input
              type="number"
              value={timeoutMs}
              onChange={(e) => setTimeoutMs(e.target.value)}
              style={{ width: '100px', marginLeft: '10px' }}
            />
          </label>
          <button onClick={fetchData} disabled={loading}>
            {loading ? 'Fetching 10MB...' : 'Fetch 10MB Data'}
          </button>
        </div>

        {error && <div className="error">{error}</div>}

        {data && (
          <div>
            <h3 className="success">Success!</h3>
            <p>Fetched <strong>{data.items.length.toLocaleString()}</strong> items in <strong>{data.duration}ms</strong></p>
            <p>Total Size: <strong>{(data.sizeBytes / (1024 * 1024)).toFixed(2)} MB</strong></p>

            <div className="data-viewer">
              {data.items.slice(0, 100).map((item, index) => (
                <div key={index} className="json-item">
                  <span style={{ color: '#aaa' }}>#{item.id}</span> <strong>{item.name}</strong>: {item.description.substring(0, 100)}...
                </div>
              ))}
              {data.items.length > 100 && (
                <div style={{ padding: '10px', textAlign: 'center', fontStyle: 'italic' }}>
                  ... and {data.items.length - 100} more items hidden for performance ...
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

export default App
