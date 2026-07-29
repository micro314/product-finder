import type { FilterOptions, ProductFilters } from '../types/filters'

type AdvancedFiltersProps = { filters: ProductFilters; options: FilterOptions | null; onChange: (filters: ProductFilters) => void; collapsible?: boolean }

export function AdvancedFilters({ filters, options, onChange, collapsible = true }: AdvancedFiltersProps) {
  const selectField = (field: keyof ProductFilters, label: string, values: string[]) => <label className="filter-field">{label}<select multiple value={(filters[field] as string[] | undefined) ?? []} onChange={(event) => onChange({ ...filters, [field]: Array.from(event.target.selectedOptions, (option) => option.value) })}>{values.map((value) => <option key={value} value={value}>{value}</option>)}</select><span className="filter-hint">Choose one or more</span></label>
  const numberField = (field: keyof ProductFilters, label: string, values: number[]) => <label className="filter-field">{label}<select value={(filters[field] as number | undefined) ?? ''} onChange={(event) => onChange({ ...filters, [field]: event.target.value === '' ? undefined : Number(event.target.value) })}><option value="">Any</option>{values.map((value) => <option key={value} value={value}>{value.toLocaleString()}</option>)}</select></label>

  const content = <div className="filter-grid">{options ? <>
    {selectField('source', 'Retailer', options.sources)}
    {selectField('manufacturer', 'Card manufacturer', options.manufacturers)}
    {selectField('chipsetManufacturer', 'Chipset manufacturer', options.chipsetManufacturers)}
    {selectField('chipset', 'Chipset', options.chipsets)}
    {selectField('memoryType', 'Memory type', options.memoryTypes)}
    <div className="filter-range-row">{numberField('minMemorySizeGb', 'Minimum VRAM (GB)', options.memorySizesGb)}{numberField('maxMemorySizeGb', 'Maximum VRAM (GB)', options.memorySizesGb)}</div>
    <div className="filter-range-row">{numberField('minBoostClockMhz', 'Minimum boost clock (MHz)', options.boostClockFrequenciesMhz)}{numberField('maxBoostClockMhz', 'Maximum boost clock (MHz)', options.boostClockFrequenciesMhz)}</div>
    <div className="filter-range-row">{numberField('minPrice', 'Minimum price', options.prices)}{numberField('maxPrice', 'Maximum price', options.prices)}</div>
  </> : <p className="filter-loading">Loading available filter values…</p>}
  </div>
  return collapsible ? <details className="advanced-filters"><summary>Advanced Filters <span>＋</span></summary>{content}</details> : <section className="advanced-filters persistent-filters"><div className="persistent-filter-heading">FILTER RESULTS</div>{content}</section>
}
