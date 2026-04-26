import { useEffect, useState } from 'react';
import { PlusCircle, Trash2, ChevronRight } from 'lucide-react';
import { getCategories, createCategory, deleteCategory } from '../services/financeService';
import type { Category } from '../types/finance';

function fmt(n: number): string {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(n);
}

// ── Recursive category row ────────────────────────────────────────────────────
function CategoryRow({
  category,
  depth,
  onDelete,
}: {
  category: Category;
  depth: number;
  onDelete: (id: number, name: string) => void;
}) {
  const isLeaf = category.children.length === 0;

  return (
    <>
      <tr className="hover:bg-slate-50 transition">
        <td className="px-5 py-3.5">
          <div className="flex items-center gap-1" style={{ paddingLeft: depth * 20 }}>
            {depth > 0 && <ChevronRight size={13} className="text-slate-300 shrink-0" />}
            <span className={`text-sm ${depth === 0 ? 'font-semibold text-slate-800' : 'text-slate-600'}`}>
              {category.name}
            </span>
          </div>
        </td>
        <td className="px-5 py-3.5 text-sm text-slate-500 tabular-nums">
          {category.percentage}%
        </td>
        <td className="px-5 py-3.5 text-sm tabular-nums font-medium text-indigo-600">
          {fmt(category.currentBalance)}
        </td>
        <td className="px-5 py-3.5 text-sm text-slate-400">
          {category.parentName ?? <span className="italic">Root</span>}
        </td>
        <td className="px-5 py-3.5 text-right">
          {isLeaf && (
            <button
              onClick={() => onDelete(category.id, category.name)}
              className="p-1.5 rounded-lg text-slate-300 hover:text-rose-500 hover:bg-rose-50 transition"
              title="Delete category"
            >
              <Trash2 size={14} />
            </button>
          )}
        </td>
      </tr>
      {category.children.map((child) => (
        <CategoryRow key={child.id} category={child} depth={depth + 1} onDelete={onDelete} />
      ))}
    </>
  );
}

// ── Main page ─────────────────────────────────────────────────────────────────
function CategoriesPage() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [loadingList, setLoadingList] = useState(true);
  const [listError, setListError] = useState('');

  // Form state
  const [name, setName] = useState('');
  const [percentage, setPercentage] = useState('');
  const [parentId, setParentId] = useState<string>('');
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState('');
  const [formSuccess, setFormSuccess] = useState('');

  // Root percentage tracking
  const rootTotal = categories.reduce((sum, c) => sum + c.percentage, 0);
  const remaining = 100 - rootTotal;

  useEffect(() => {
    fetchCategories();
  }, []);

  async function fetchCategories() {
    setLoadingList(true);
    setListError('');
    try {
      const data = await getCategories();
      setCategories(data);
    } catch {
      setListError('Could not load categories.');
    } finally {
      setLoadingList(false);
    }
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setFormError('');
    setFormSuccess('');
    setSubmitting(true);
    try {
      await createCategory({
        name: name.trim(),
        percentage: parseFloat(percentage),
        parentId: parentId ? parseInt(parentId) : null,
      });
      setName('');
      setPercentage('');
      setParentId('');
      setFormSuccess('Category created successfully.');
      await fetchCategories();
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        'Failed to create category.';
      setFormError(msg);
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDelete(id: number, catName: string) {
    if (!confirm(`Delete category "${catName}"? This cannot be undone.`)) return;
    try {
      await deleteCategory(id);
      await fetchCategories();
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        'Failed to delete category.';
      alert(msg);
    }
  }

  // Flat list of all categories for the parent selector
  function flattenCategories(cats: Category[]): Category[] {
    return cats.flatMap((c) => [c, ...flattenCategories(c.children)]);
  }
  const allFlat = flattenCategories(categories);

  return (
    <div>
      <h1 className="text-2xl font-semibold text-slate-900 mb-1">Categories</h1>
      <p className="text-sm text-slate-500">
        Define your budget buckets — root categories must total exactly 100%
      </p>

      {/* ── Root allocation bar ───────────────────────────────── */}
      <div className="mt-6 bg-white rounded-xl border border-slate-100 shadow-sm p-5">
        <div className="flex items-center justify-between mb-2">
          <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
            Root allocation
          </span>
          <span className={`text-xs font-semibold tabular-nums ${
            rootTotal === 100 ? 'text-emerald-600' : rootTotal > 100 ? 'text-rose-600' : 'text-amber-600'
          }`}>
            {rootTotal}% / 100%
          </span>
        </div>
        <div className="w-full h-2 bg-slate-100 rounded-full overflow-hidden">
          <div
            className={`h-full rounded-full transition-all ${
              rootTotal === 100 ? 'bg-emerald-500' : rootTotal > 100 ? 'bg-rose-500' : 'bg-indigo-500'
            }`}
            style={{ width: `${Math.min(rootTotal, 100)}%` }}
          />
        </div>
        {rootTotal === 100 ? (
          <p className="mt-1.5 text-xs text-emerald-600 font-medium">Ready — 100% allocated</p>
        ) : (
          <p className="mt-1.5 text-xs text-amber-600">{remaining}% remaining to allocate at root level</p>
        )}
      </div>

      {/* ── Add Category Form ─────────────────────────────────── */}
      <div className="mt-4 bg-white rounded-xl border border-slate-100 shadow-sm p-6">
        <h2 className="text-sm font-semibold text-slate-700 mb-4">Add Category</h2>

        {formError && (
          <div className="mb-4 px-4 py-3 rounded-xl bg-rose-50 border border-rose-200 text-rose-600 text-sm">
            {formError}
          </div>
        )}
        {formSuccess && (
          <div className="mb-4 px-4 py-3 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-700 text-sm">
            {formSuccess}
          </div>
        )}

        <form onSubmit={handleSubmit} className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          {/* Name */}
          <div>
            <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5">
              Name <span className="text-rose-500">*</span>
            </label>
            <input
              type="text"
              placeholder="e.g. Housing, Food"
              value={name}
              onChange={e => setName(e.target.value)}
              required
              className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent focus:bg-white transition"
            />
          </div>

          {/* Percentage */}
          <div>
            <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5">
              Percentage <span className="text-rose-500">*</span>
            </label>
            <div className="relative">
              <input
                type="number"
                min="0"
                max="100"
                step="0.01"
                placeholder="0"
                value={percentage}
                onChange={e => setPercentage(e.target.value)}
                required
                className="w-full px-4 pr-8 py-3 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent focus:bg-white transition"
              />
              <span className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 text-sm">%</span>
            </div>
          </div>

          {/* Parent */}
          <div>
            <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5">
              Parent <span className="text-slate-300">(optional)</span>
            </label>
            <select
              value={parentId}
              onChange={e => setParentId(e.target.value)}
              className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent focus:bg-white transition"
            >
              <option value="">— Root category —</option>
              {allFlat.map(c => (
                <option key={c.id} value={c.id}>
                  {c.parentId ? `  ↳ ${c.name}` : c.name}
                </option>
              ))}
            </select>
          </div>

          {/* Submit */}
          <div className="sm:col-span-3 flex justify-end">
            <button
              type="submit"
              disabled={submitting}
              className="inline-flex items-center gap-2 px-5 py-2.5 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-60 text-white text-sm font-medium rounded-xl transition"
            >
              <PlusCircle size={16} />
              {submitting ? 'Creating…' : 'Add Category'}
            </button>
          </div>
        </form>
      </div>

      {/* ── Categories Table ──────────────────────────────────── */}
      <div className="mt-6">
        <h2 className="text-base font-semibold text-slate-800 mb-3">Your Categories</h2>
        <div className="bg-white rounded-xl border border-slate-100 shadow-sm overflow-hidden">
          {loadingList ? (
            <div className="divide-y divide-slate-50">
              {[...Array(4)].map((_, i) => (
                <div key={i} className="flex items-center gap-4 px-5 py-4">
                  <div className="h-4 w-32 bg-slate-100 rounded animate-pulse" />
                  <div className="h-4 w-16 bg-slate-100 rounded animate-pulse" />
                  <div className="h-4 w-24 bg-slate-100 rounded animate-pulse" />
                  <div className="h-4 flex-1 bg-slate-100 rounded animate-pulse" />
                </div>
              ))}
            </div>
          ) : listError ? (
            <div className="flex items-center justify-center h-24 text-sm text-rose-500">{listError}</div>
          ) : categories.length === 0 ? (
            <div className="flex items-center justify-center h-32 text-sm text-slate-400">
              No categories yet. Use the form above to create your first budget category.
            </div>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-slate-100 text-left">
                  <th className="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Name</th>
                  <th className="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">%</th>
                  <th className="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Balance</th>
                  <th className="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Parent</th>
                  <th className="px-5 py-3" />
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-50">
                {categories.map((cat) => (
                  <CategoryRow key={cat.id} category={cat} depth={0} onDelete={handleDelete} />
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}

export default CategoriesPage;
