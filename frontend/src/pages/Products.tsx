import React, { useState, useEffect, useCallback } from 'react';
import { Search, Filter, ArrowUpDown, X, ChevronLeft, ChevronRight, Zap, ShieldCheck, Truck, Headphones } from 'lucide-react';
import { productService, ProductQueryParams } from '../services/productService';
import { Product, Category, PageResponse } from '../types';
import { ProductCard } from '../components/ProductCard';

export const Products: React.FC = () => {
  const [pageData, setPageData] = useState<PageResponse<Product> | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Filters
  const [keyword, setKeyword] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<number | undefined>(undefined);
  const [minPrice, setMinPrice] = useState<string>('');
  const [maxPrice, setMaxPrice] = useState<string>('');
  const [sortOption, setSortOption] = useState<string>('id,DESC');
  const [currentPage, setCurrentPage] = useState(0);

  // Load categories
  useEffect(() => {
    productService
      .getCategories()
      .then(setCategories)
      .catch((err) => console.error('Failed to load categories', err));
  }, []);

  const loadProducts = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      const [sortBy, sortDirection] = sortOption.split(',');

      const params: ProductQueryParams = {
        page: currentPage,
        size: 8,
        sortBy: sortBy || 'id',
        sortDirection: (sortDirection as 'ASC' | 'DESC') || 'DESC',
      };

      if (keyword.trim()) params.keyword = keyword.trim();
      if (selectedCategory) params.categoryId = selectedCategory;
      if (minPrice) params.minPrice = parseFloat(minPrice);
      if (maxPrice) params.maxPrice = parseFloat(maxPrice);

      const data = await productService.getProducts(params);
      setPageData(data);
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Failed to load products';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, [currentPage, keyword, selectedCategory, minPrice, maxPrice, sortOption]);

  useEffect(() => {
    loadProducts();
  }, [loadProducts]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setCurrentPage(0);
    loadProducts();
  };

  const handleCategoryClick = (catId?: number) => {
    setSelectedCategory(catId);
    setCurrentPage(0);
  };

  const handleResetFilters = () => {
    setKeyword('');
    setSelectedCategory(undefined);
    setMinPrice('');
    setMaxPrice('');
    setSortOption('id,DESC');
    setCurrentPage(0);
  };

  return (
    <div>
      {/* Hero Section */}
      <div
        style={{
          background: 'linear-gradient(135deg, rgba(79, 70, 229, 0.12) 0%, rgba(6, 182, 212, 0.05) 100%)',
          border: '1px solid var(--border-subtle)',
          borderRadius: 'var(--radius-lg)',
          padding: '2.5rem 2rem',
          marginBottom: '2rem',
          position: 'relative',
          overflow: 'hidden',
        }}
      >
        <div style={{ maxWidth: '650px' }}>
          <div
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.4rem',
              padding: '4px 10px',
              borderRadius: 'var(--radius-full)',
              background: 'var(--primary-light)',
              color: '#818cf8',
              fontSize: '0.78rem',
              fontWeight: 700,
              marginBottom: '0.75rem',
            }}
          >
            ENTERPRISE COMMERCE ENGINE
          </div>
          <h1 style={{ fontSize: '2.25rem', marginBottom: '0.5rem', lineHeight: 1.2 }}>
            Next-Gen Hardware & Peripherals
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.98rem' }}>
            High-performance engineering gear backed by distributed microservices, Redis caching, and real-time inventory locking.
          </p>
        </div>
      </div>

      {/* Enterprise Trust Highlights */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
          gap: '1rem',
          marginBottom: '2rem',
        }}
      >
        <div className="card" style={{ padding: '1rem 1.25rem', display: 'flex', alignItems: 'center', gap: '0.85rem' }}>
          <div style={{ width: 40, height: 40, borderRadius: 'var(--radius-md)', background: 'rgba(79, 70, 229, 0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#818cf8', flexShrink: 0 }}>
            <Zap size={20} />
          </div>
          <div>
            <div style={{ fontWeight: 700, fontSize: '0.88rem', color: '#fff' }}>Real-Time Stocks</div>
            <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>Atomic Redis & MySQL locks</div>
          </div>
        </div>

        <div className="card" style={{ padding: '1rem 1.25rem', display: 'flex', alignItems: 'center', gap: '0.85rem' }}>
          <div style={{ width: 40, height: 40, borderRadius: 'var(--radius-md)', background: 'rgba(16, 185, 129, 0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--emerald)', flexShrink: 0 }}>
            <ShieldCheck size={20} />
          </div>
          <div>
            <div style={{ fontWeight: 700, fontSize: '0.88rem', color: '#fff' }}>2-Year Warranty</div>
            <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>Full enterprise hardware guarantee</div>
          </div>
        </div>

        <div className="card" style={{ padding: '1rem 1.25rem', display: 'flex', alignItems: 'center', gap: '0.85rem' }}>
          <div style={{ width: 40, height: 40, borderRadius: 'var(--radius-md)', background: 'rgba(56, 189, 248, 0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#38bdf8', flexShrink: 0 }}>
            <Truck size={20} />
          </div>
          <div>
            <div style={{ fontWeight: 700, fontSize: '0.88rem', color: '#fff' }}>Priority Express</div>
            <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>Insured 24-hour tracked dispatch</div>
          </div>
        </div>

        <div className="card" style={{ padding: '1rem 1.25rem', display: 'flex', alignItems: 'center', gap: '0.85rem' }}>
          <div style={{ width: 40, height: 40, borderRadius: 'var(--radius-md)', background: 'rgba(245, 158, 11, 0.15)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#f59e0b', flexShrink: 0 }}>
            <Headphones size={20} />
          </div>
          <div>
            <div style={{ fontWeight: 700, fontSize: '0.88rem', color: '#fff' }}>24/7 Support</div>
            <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>Direct access to tech specialists</div>
          </div>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div
        className="card"
        style={{
          marginBottom: '2rem',
          padding: '1.25rem',
          display: 'flex',
          flexDirection: 'column',
          gap: '1rem',
        }}
      >
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '1rem', alignItems: 'center' }}>
          {/* Keyword Search */}
          <form onSubmit={handleSearchSubmit} style={{ flex: '1 1 260px', position: 'relative' }}>
            <input
              type="text"
              className="form-input"
              placeholder="Search products by name or description..."
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              style={{ paddingLeft: '2.4rem' }}
            />
            <Search
              size={16}
              color="var(--text-dim)"
              style={{ position: 'absolute', left: '0.85rem', top: '50%', transform: 'translateY(-50%)' }}
            />
          </form>

          {/* Min Price */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', width: '130px' }}>
            <span style={{ fontSize: '0.82rem', color: 'var(--text-dim)' }}>$</span>
            <input
              type="number"
              className="form-input"
              placeholder="Min Price"
              value={minPrice}
              onChange={(e) => {
                setMinPrice(e.target.value);
                setCurrentPage(0);
              }}
              min="0"
              style={{ padding: '0.6rem 0.5rem' }}
            />
          </div>

          {/* Max Price */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', width: '130px' }}>
            <span style={{ fontSize: '0.82rem', color: 'var(--text-dim)' }}>$</span>
            <input
              type="number"
              className="form-input"
              placeholder="Max Price"
              value={maxPrice}
              onChange={(e) => {
                setMaxPrice(e.target.value);
                setCurrentPage(0);
              }}
              min="0"
              style={{ padding: '0.6rem 0.5rem' }}
            />
          </div>

          {/* Sort Dropdown */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <ArrowUpDown size={15} color="var(--text-dim)" />
            <select
              className="form-select"
              value={sortOption}
              onChange={(e) => {
                setSortOption(e.target.value);
                setCurrentPage(0);
              }}
              style={{ width: '170px' }}
            >
              <option value="id,DESC">Newest First</option>
              <option value="price,ASC">Price: Low to High</option>
              <option value="price,DESC">Price: High to Low</option>
              <option value="name,ASC">Name: A to Z</option>
            </select>
          </div>

          {(keyword || selectedCategory || minPrice || maxPrice) && (
            <button onClick={handleResetFilters} className="btn btn-outline btn-sm" title="Clear all filters">
              <X size={14} /> Clear
            </button>
          )}
        </div>

        {/* Category Pills */}
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', alignItems: 'center', paddingTop: '0.5rem', borderTop: '1px solid var(--border-subtle)' }}>
          <span style={{ fontSize: '0.8rem', color: 'var(--text-dim)', display: 'flex', alignItems: 'center', gap: '0.25rem', marginRight: '0.5rem' }}>
            <Filter size={13} /> Categories:
          </span>
          <button
            onClick={() => handleCategoryClick(undefined)}
            className={`btn btn-sm ${selectedCategory === undefined ? 'btn-primary' : 'btn-secondary'}`}
          >
            All Products
          </button>
          {categories.map((cat) => (
            <button
              key={cat.id}
              onClick={() => handleCategoryClick(cat.id)}
              className={`btn btn-sm ${selectedCategory === cat.id ? 'btn-primary' : 'btn-secondary'}`}
            >
              {cat.name}
            </button>
          ))}
        </div>
      </div>

      {/* Error Alert */}
      {error && (
        <div className="alert alert-error">
          <span>{error}</span>
        </div>
      )}

      {/* Product Grid / Loading / Empty */}
      {loading ? (
        <div className="flex-center" style={{ minHeight: '40vh', flexDirection: 'column', gap: '1rem' }}>
          <div className="spinner" style={{ width: 36, height: 36 }} />
          <p className="text-muted text-sm">Querying microservices...</p>
        </div>
      ) : !pageData || pageData.content.length === 0 ? (
        <div className="card flex-center" style={{ minHeight: '30vh', flexDirection: 'column', gap: '1rem', padding: '3rem' }}>
          <p style={{ fontSize: '1.1rem', fontWeight: 600 }}>No products matched your criteria.</p>
          <p className="text-muted text-sm">Try broadening your search term or clearing active price filters.</p>
          <button onClick={handleResetFilters} className="btn btn-secondary btn-sm">
            Reset All Filters
          </button>
        </div>
      ) : (
        <>
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))',
              gap: '1.5rem',
              marginBottom: '2.5rem',
            }}
          >
            {pageData.content.map((product) => (
              <ProductCard key={product.id} product={product} />
            ))}
          </div>

          {/* Pagination Controls */}
          {(() => {
            const pageNum = pageData.number ?? pageData.pageNumber ?? 0;
            const pageSize = pageData.size ?? pageData.pageSize ?? 8;
            const startItem = pageData.totalElements === 0 ? 0 : pageNum * pageSize + 1;
            const endItem = Math.min((pageNum + 1) * pageSize, pageData.totalElements);

            return (
              <div className="flex-between" style={{ padding: '1.25rem 0', borderTop: '1px solid var(--border-subtle)', flexWrap: 'wrap', gap: '1rem' }}>
                <div className="text-muted text-sm">
                  Showing <strong style={{ color: 'var(--text-main)' }}>{startItem} - {endItem}</strong> of{' '}
                  <strong style={{ color: 'var(--text-main)' }}>{pageData.totalElements}</strong> products
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <button
                    disabled={pageData.first || loading}
                    onClick={() => setCurrentPage((p) => Math.max(0, p - 1))}
                    className="btn btn-secondary btn-sm"
                  >
                    <ChevronLeft size={16} /> Previous
                  </button>

                  <span style={{ fontSize: '0.88rem', fontWeight: 600, padding: '0 0.75rem', color: 'var(--primary-hover)' }}>
                    Page {pageNum + 1} of {pageData.totalPages || 1}
                  </span>

                  <button
                    disabled={pageData.last || loading}
                    onClick={() => setCurrentPage((p) => p + 1)}
                    className="btn btn-secondary btn-sm"
                  >
                    Next <ChevronRight size={16} />
                  </button>
                </div>
              </div>
            );
          })()}
        </>
      )}
    </div>
  );
};
