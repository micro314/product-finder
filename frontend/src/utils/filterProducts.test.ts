import { describe, it, expect } from 'vitest';
import { matchesProductFilters, filterOptionsForResults } from './filterProducts';
import type { Product } from '../types/product';
import type { ProductFilters } from '../types/filters';

const sampleProducts: Product[] = [
  {
    source: 'Abt',
    externalId: '1',
    name: 'ASUS ROG Strix GeForce RTX 4090 OC Edition',
    manufacturer: 'ASUS',
    chipset: 'GeForce RTX 4090',
    chipsetManufacturer: 'NVIDIA',
    memorySizeGb: 24,
    memoryType: 'GDDR6X',
    boostClockMhz: 2640,
    description: 'Flagship NVIDIA card by ASUS',
    price: 1999,
    currency: 'USD',
    productUrl: 'https://example.com/1',
  },
  {
    source: 'Newegg',
    externalId: '2',
    name: 'Gigabyte GeForce RTX 4080 SUPER Gaming OC',
    manufacturer: 'Gigabyte',
    chipset: 'GeForce RTX 4080 SUPER',
    chipsetManufacturer: 'NVIDIA',
    memorySizeGb: 16,
    memoryType: 'GDDR6X',
    boostClockMhz: 2550,
    description: 'High performance gaming GPU',
    price: 999,
    currency: 'USD',
    productUrl: 'https://example.com/2',
  },
  {
    source: 'B&H Photo Video',
    externalId: '3',
    name: 'MSI Radeon RX 7900 XTX Gaming Trio Classic',
    manufacturer: 'MSI',
    chipset: 'Radeon RX 7900 XTX',
    chipsetManufacturer: 'AMD',
    memorySizeGb: 24,
    memoryType: 'GDDR6',
    boostClockMhz: 2500,
    description: 'Top AMD Radeon graphics card',
    price: 949,
    currency: 'USD',
    productUrl: 'https://example.com/3',
  },
];

describe('matchesProductFilters', () => {
  it('matches when name filter matches graphics card name case-insensitively', () => {
    const filters: ProductFilters = { name: 'rog strix' };
    expect(matchesProductFilters(sampleProducts[0], filters)).toBe(true);
    expect(matchesProductFilters(sampleProducts[1], filters)).toBe(false);
    expect(matchesProductFilters(sampleProducts[2], filters)).toBe(false);
  });

  it('matches substring in graphics card name', () => {
    const filters: ProductFilters = { name: 'Gaming' };
    expect(matchesProductFilters(sampleProducts[0], filters)).toBe(false);
    expect(matchesProductFilters(sampleProducts[1], filters)).toBe(true);
    expect(matchesProductFilters(sampleProducts[2], filters)).toBe(true);
  });

  it('matches all products when name filter is undefined or empty', () => {
    expect(matchesProductFilters(sampleProducts[0], {})).toBe(true);
    expect(matchesProductFilters(sampleProducts[0], { name: '' })).toBe(true);
    expect(matchesProductFilters(sampleProducts[0], { name: '   ' })).toBe(true);
  });

  it('ignores name filter when excluded field is name', () => {
    const filters: ProductFilters = { name: 'Nonexistent Card Name' };
    expect(matchesProductFilters(sampleProducts[0], filters, 'name')).toBe(true);
  });

  it('combines name filter with other filters', () => {
    const matchingFilters: ProductFilters = {
      name: 'GeForce',
      manufacturer: ['Gigabyte'] as unknown as string,
      minMemorySizeGb: 16,
    };
    expect(matchesProductFilters(sampleProducts[1], matchingFilters)).toBe(true);
    expect(matchesProductFilters(sampleProducts[0], matchingFilters)).toBe(false);
  });
});

describe('filterOptionsForResults', () => {
  it('narrows down available filter options based on name filter', () => {
    const options = filterOptionsForResults(sampleProducts, { name: 'Radeon' });
    expect(options.manufacturers).toEqual(['MSI']);
    expect(options.chipsetManufacturers).toEqual(['AMD']);
    expect(options.chipsets).toEqual(['Radeon RX 7900 XTX']);
  });
});
