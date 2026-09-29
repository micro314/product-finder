import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AdvancedFilters } from './AdvancedFilters';
import type { FilterOptions, ProductFilters } from '../types/filters';

const mockOptions: FilterOptions = {
  sources: ['Abt', 'B&H Photo Video', 'Newegg'],
  manufacturers: ['ASUS', 'Gigabyte', 'MSI'],
  chipsetManufacturers: ['AMD', 'Intel', 'NVIDIA'],
  chipsets: ['GeForce RTX 4090', 'Radeon RX 7900 XTX'],
  memoryTypes: ['GDDR6', 'GDDR6X'],
  memorySizesGb: [8, 16, 24],
  boostClockFrequenciesMhz: [2200, 2500, 2800],
  prices: [500, 1000, 1500, 2000],
};

describe('AdvancedFilters', () => {
  it('renders loading state when options are null', () => {
    render(
      <AdvancedFilters
        filters={{}}
        options={null}
        onChange={vi.fn()}
      />
    );

    expect(screen.getByText('Loading available filter values…')).toBeInTheDocument();
    expect(screen.queryByLabelText(/Retailer/i)).not.toBeInTheDocument();
  });

  it('renders in collapsible mode by default', () => {
    const { container } = render(
      <AdvancedFilters
        filters={{}}
        options={mockOptions}
        onChange={vi.fn()}
      />
    );

    const detailsElement = container.querySelector('details.advanced-filters');
    expect(detailsElement).toBeInTheDocument();
    expect(screen.getByText(/Advanced Filters/i)).toBeInTheDocument();
    expect(screen.queryByText('FILTER RESULTS')).not.toBeInTheDocument();
  });

  it('renders in persistent (non-collapsible) mode when collapsible is false', () => {
    const { container } = render(
      <AdvancedFilters
        filters={{}}
        options={mockOptions}
        onChange={vi.fn()}
        collapsible={false}
      />
    );

    const sectionElement = container.querySelector('section.advanced-filters.persistent-filters');
    expect(sectionElement).toBeInTheDocument();
    expect(screen.getByText('FILTER RESULTS')).toBeInTheDocument();
    expect(container.querySelector('details')).not.toBeInTheDocument();
  });

  it('renders all filter fields and options when options are loaded', () => {
    render(
      <AdvancedFilters
        filters={{}}
        options={mockOptions}
        onChange={vi.fn()}
      />
    );

    // Text field labels and element kinds
    const cardNameInput = screen.getByLabelText(/^Card name/i);
    expect(cardNameInput).toBeInTheDocument();
    expect(cardNameInput).toBeInstanceOf(HTMLInputElement);
    expect(cardNameInput).toHaveAttribute('type', 'text');

    // Multi-select labels and element kinds
    const multiSelectFields = [
      screen.getByLabelText(/^Retailer/i),
      screen.getByLabelText(/^Card manufacturer/i),
      screen.getByLabelText(/^Chipset manufacturer/i),
      screen.getByLabelText((content) =>
        content.startsWith('Chipset') && !content.startsWith('Chipset manufacturer')
      ),
      screen.getByLabelText(/^Memory type/i),
    ];

    for (const select of multiSelectFields) {
      expect(select).toBeInTheDocument();
      expect(select).toBeInstanceOf(HTMLSelectElement);
      expect((select as HTMLSelectElement).multiple).toBe(true);
      expect(select).toHaveAttribute('multiple');
    }

    // Range / numeric filter labels and element kinds (single select)
    const numericSelectLabels = [
      'Minimum VRAM (GB)',
      'Maximum VRAM (GB)',
      'Minimum boost clock (MHz)',
      'Maximum boost clock (MHz)',
      'Minimum price',
      'Maximum price',
    ];

    for (const label of numericSelectLabels) {
      const select = screen.getByLabelText(label);
      expect(select).toBeInTheDocument();
      expect(select).toBeInstanceOf(HTMLSelectElement);
      expect((select as HTMLSelectElement).multiple).toBe(false);
      expect(select).not.toHaveAttribute('multiple');
    }

    // Verify select options are populated
    expect(screen.getByRole('option', { name: 'Abt' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'ASUS' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'GeForce RTX 4090' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'GDDR6' })).toBeInTheDocument();

    // Multi-select hint
    const hints = screen.getAllByText('Choose one or more');
    expect(hints).toHaveLength(5);
  });

  it('reflects active filter values passed via props', () => {
    const activeFilters: ProductFilters = {
      name: 'ROG Strix',
      source: ['Abt', 'Newegg'] as unknown as string,
      manufacturer: ['ASUS'] as unknown as string,
      minMemorySizeGb: 16,
      maxPrice: 1500,
    };

    render(
      <AdvancedFilters
        filters={activeFilters}
        options={mockOptions}
        onChange={vi.fn()}
      />
    );

    const nameInput = screen.getByLabelText(/Card name/i) as HTMLInputElement;
    expect(nameInput.value).toBe('ROG Strix');

    const retailerSelect = screen.getByLabelText(/Retailer/i) as HTMLSelectElement;
    const selectedRetailers = Array.from(retailerSelect.selectedOptions).map((o) => o.value);
    expect(selectedRetailers).toEqual(['Abt', 'Newegg']);

    const manufacturerSelect = screen.getByLabelText(/Card manufacturer/i) as HTMLSelectElement;
    const selectedManufacturers = Array.from(manufacturerSelect.selectedOptions).map((o) => o.value);
    expect(selectedManufacturers).toEqual(['ASUS']);

    const minVramSelect = screen.getByLabelText(/Minimum VRAM \(GB\)/i) as HTMLSelectElement;
    expect(minVramSelect.value).toBe('16');

    const maxPriceSelect = screen.getByLabelText(/Maximum price/i) as HTMLSelectElement;
    expect(maxPriceSelect.value).toBe('1500');
  });

  it('calls onChange with updated text when card name filter changes', () => {
    const handleChange = vi.fn();
    const initialFilters: ProductFilters = { minPrice: 500 };

    render(
      <AdvancedFilters
        filters={initialFilters}
        options={mockOptions}
        onChange={handleChange}
      />
    );

    const nameInput = screen.getByLabelText(/Card name/i);
    fireEvent.change(nameInput, { target: { value: 'Gaming OC' } });

    expect(handleChange).toHaveBeenCalledWith({
      minPrice: 500,
      name: 'Gaming OC',
    });
  });

  it('calls onChange with undefined for name when card name filter is cleared', () => {
    const handleChange = vi.fn();
    const initialFilters: ProductFilters = { name: 'Gaming OC', minPrice: 500 };

    render(
      <AdvancedFilters
        filters={initialFilters}
        options={mockOptions}
        onChange={handleChange}
      />
    );

    const nameInput = screen.getByLabelText(/Card name/i);
    fireEvent.change(nameInput, { target: { value: '' } });

    expect(handleChange).toHaveBeenCalledWith({
      name: undefined,
      minPrice: 500,
    });
  });

  it('calls onChange with updated array when multi-select option is selected', async () => {
    const user = userEvent.setup();
    const handleChange = vi.fn();
    const initialFilters: ProductFilters = { minPrice: 500 };

    render(
      <AdvancedFilters
        filters={initialFilters}
        options={mockOptions}
        onChange={handleChange}
      />
    );

    const retailerSelect = screen.getByLabelText(/^Retailer/i);
    await user.selectOptions(retailerSelect, 'Abt');

    expect(handleChange).toHaveBeenCalledWith({
      minPrice: 500,
      source: ['Abt'],
    });
  });

  it('updates selection when multiple options are selected', () => {
    const handleChange = vi.fn();
    const initialFilters: ProductFilters = { minPrice: 500 };

    render(
      <AdvancedFilters
        filters={initialFilters}
        options={mockOptions}
        onChange={handleChange}
      />
    );

    const retailerSelect = screen.getByLabelText(/^Retailer/i) as HTMLSelectElement;
    const abtOption = screen.getByRole('option', { name: 'Abt' }) as HTMLOptionElement;
    const neweggOption = screen.getByRole('option', { name: 'Newegg' }) as HTMLOptionElement;

    abtOption.selected = true;
    neweggOption.selected = true;
    retailerSelect.dispatchEvent(new Event('change', { bubbles: true }));

    expect(handleChange).toHaveBeenCalledWith({
      minPrice: 500,
      source: ['Abt', 'Newegg'],
    });
  });

  it('calls onChange with updated number value when numeric filter changes', async () => {
    const user = userEvent.setup();
    const handleChange = vi.fn();
    const initialFilters: ProductFilters = { minPrice: 500 };

    render(
      <AdvancedFilters
        filters={initialFilters}
        options={mockOptions}
        onChange={handleChange}
      />
    );

    const minVramSelect = screen.getByLabelText(/Minimum VRAM \(GB\)/i);
    await user.selectOptions(minVramSelect, '16');

    expect(handleChange).toHaveBeenCalledWith({
      minPrice: 500,
      minMemorySizeGb: 16,
    });
  });

  it('calls onChange with undefined for field when numeric filter is set back to Any', async () => {
    const user = userEvent.setup();
    const handleChange = vi.fn();
    const initialFilters: ProductFilters = { minMemorySizeGb: 16, maxPrice: 1500 };

    render(
      <AdvancedFilters
        filters={initialFilters}
        options={mockOptions}
        onChange={handleChange}
      />
    );

    const minVramSelect = screen.getByLabelText(/Minimum VRAM \(GB\)/i);
    await user.selectOptions(minVramSelect, '');

    expect(handleChange).toHaveBeenCalledWith({
      minMemorySizeGb: undefined,
      maxPrice: 1500,
    });
  });
});
