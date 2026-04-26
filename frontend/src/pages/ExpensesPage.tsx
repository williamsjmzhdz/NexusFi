import { useEffect, useState } from 'react';
import { PlusCircle } from 'lucide-react';
import { getCategories, getExpenses, recordExpense } from '../services/financeService';
import type { Category, ExpenseRecord } from '../types/finance';

function fmt(amount: number): string {
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);
}

function fmtDate(dateStr: string): string {
  return new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

const today = new Date().toISOString().split('T')[0];

// Flatten category tree and keep only leaf nodes (no children) — those hold balances
function getLeafCategories(cats: Category[]): Category[] {
  return cats.flatMap((c) =>
    c.children.length === 0 ? [c] : getLeafCategories(c.children)
  );
}

function ExpensesPage() {
  // ── Form state ──────────────────────────────────────────────
  const [amount, setAmount] = useState('');
  const [merchant, setMerchant] = useState('');
  const [description, setDescription] = useState('');
  const [expenseDate, setExpenseDate] = useState(today);
  const [categoryId, setCategoryId] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [formError, setFormError] = useState('');

  // ── Category options ─────────────────────────────────────────
  const [leafCategories, setLeafCategories] = useState<Category[]>([]);
  const [loadingCats, setLoadingCats] = useState(true);

  // ── Expense list ─────────────────────────────────────────────
  const [expenses, setExpenses] = useState<ExpenseRecord[]>([]);
  const [loadingList, setLoadingList] = useState(true);
  const [listError, setListError] = useState('');

  useEffect(() => {
    async function init() {
      try {
        const [cats, exps] = await Promise.all([getCategories(), getExpenses()]);
        setLeafCategories(getLeafCategories(cats));
        const sorted = [...exps].sort(
          (a, b) => new Date(b.expenseDate).getTime() - new Date(a.expenseDate).getTime()
        );
        setExpenses(sorted);
      } catch {
        setListError('Could not load data.');
      } finally {
        setLoadingCats(false);
        setLoadingList(false);
      }
    }
    init();
  }, []);

  async function fetchExpenses() {
    try {
      const exps = await getExpenses();
      const sorted = [...exps].sort(
        (a, b) => new Date(b.expenseDate).getTime() - new Date(a.expenseDate).getTime()
      );
      setExpenses(sorted);
    } catch {
      setListError('Could not refresh expense list.');
    }
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setFormError('');
    setSubmitting(true);
    try {
      await recordExpense({
        amount: parseFloat(amount),
        merchant: merchant.trim(),
        description: description.trim() || undefined,
        expenseDate,
        categoryId: parseInt(categoryId),
      });
      setAmount('');
      setMerchant('');
      setDescription('');
      setExpenseDate(today);
      setCategoryId('');
      await fetchExpenses();
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        'Failed to record expense. Please try again.';
      setFormError(msg);
    } finally {
      setSubmitting(false);
    }
  }

  const noCats = !loadingCats && leafCategories.length === 0;

  return (
    <div>
      <h1 className="text-2xl font-semibold text-slate-900 mb-1">Expenses</h1>
      <p className="text-sm text-slate-500">Record spending and view your expense history</p>

      {/* ── Add Expense Form ──────────────────────────────────── */}
      <div className="mt-6 bg-white rounded-xl border border-slate-100 shadow-sm p-6">
        <h2 className="text-sm font-semibold text-slate-700 mb-4">Record New Expense</h2>

        {noCats && (
          <div className="mb-4 px-4 py-3 rounded-xl bg-amber-50 border border-amber-200 text-amber-700 text-sm">
            No categories found. Go to <strong>Categories</strong> and set up your budget first.
          </div>
        )}

        {formError && (
          <div className="mb-4 px-4 py-3 rounded-xl bg-rose-50 border border-rose-200 text-rose-600 text-sm">
            {formError}
          </div>
        )}

        <form onSubmit={handleSubmit} className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          {/* Amount */}
          <div>
            <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5">
              Amount <span className="text-rose-500">*</span>
            </label>
            <div className="relative">
              <span className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-sm">$</span>
              <input
                type="number"
                min="0.01"
                step="0.01"
                placeholder="0.00"
                value={amount}
                onChange={e => setAmount(e.target.value)}
                required
                className="w-full pl-7 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent focus:bg-white transition"
              />
            </div>
          </div>

          {/* Merchant */}
          <div>
            <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5">
              Merchant <span className="text-rose-500">*</span>
            </label>
            <input
              type="text"
              placeholder="e.g. Walmart, Netflix"
              value={merchant}
              onChange={e => setMerchant(e.target.value)}
              required
              className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent focus:bg-white transition"
            />
          </div>

          {/* Category */}
          <div>
            <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5">
              Category <span className="text-rose-500">*</span>
            </label>
            <select
              value={categoryId}
              onChange={e => setCategoryId(e.target.value)}
              required
              disabled={loadingCats || noCats}
              className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent focus:bg-white transition disabled:opacity-50"
            >
              <option value="">— Select category —</option>
              {leafCategories.map(c => (
                <option key={c.id} value={c.id}>
                  {c.parentName ? `${c.parentName} › ${c.name}` : c.name}
                </option>
              ))}
            </select>
          </div>

          {/* Date */}
          <div>
            <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5">
              Date <span className="text-rose-500">*</span>
            </label>
            <input
              type="date"
              value={expenseDate}
              onChange={e => setExpenseDate(e.target.value)}
              required
              className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent focus:bg-white transition"
            />
          </div>

          {/* Description */}
          <div className="sm:col-span-2">
            <label className="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5">
              Description <span className="text-slate-300">(optional)</span>
            </label>
            <input
              type="text"
              placeholder="Optional note"
              value={description}
              onChange={e => setDescription(e.target.value)}
              className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent focus:bg-white transition"
            />
          </div>

          {/* Submit */}
          <div className="sm:col-span-2 flex justify-end">
            <button
              type="submit"
              disabled={submitting || noCats}
              className="inline-flex items-center gap-2 px-5 py-2.5 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-60 text-white text-sm font-medium rounded-xl transition"
            >
              <PlusCircle size={16} />
              {submitting ? 'Recording…' : 'Record Expense'}
            </button>
          </div>
        </form>
      </div>

      {/* ── Expense History ───────────────────────────────────── */}
      <div className="mt-6">
        <h2 className="text-base font-semibold text-slate-800 mb-3">Expense History</h2>
        <div className="bg-white rounded-xl border border-slate-100 shadow-sm overflow-hidden">
          {loadingList ? (
            <div className="divide-y divide-slate-50">
              {[...Array(4)].map((_, i) => (
                <div key={i} className="flex items-center gap-4 px-5 py-4">
                  <div className="h-4 w-24 bg-slate-100 rounded animate-pulse" />
                  <div className="h-4 w-32 bg-slate-100 rounded animate-pulse" />
                  <div className="h-4 flex-1 bg-slate-100 rounded animate-pulse" />
                  <div className="h-4 w-28 bg-slate-100 rounded animate-pulse" />
                  <div className="h-4 w-20 bg-slate-100 rounded animate-pulse" />
                </div>
              ))}
            </div>
          ) : listError ? (
            <div className="flex items-center justify-center h-24 text-sm text-rose-500">{listError}</div>
          ) : expenses.length === 0 ? (
            <div className="flex items-center justify-center h-32 text-sm text-slate-400">
              No expenses recorded yet. Use the form above to add your first entry.
            </div>
          ) : (
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-slate-100 text-left">
                  <th className="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Date</th>
                  <th className="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Merchant</th>
                  <th className="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Description</th>
                  <th className="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider">Category</th>
                  <th className="px-5 py-3 text-xs font-semibold text-slate-400 uppercase tracking-wider text-right">Amount</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-50">
                {expenses.map((exp) => (
                  <tr key={exp.id} className="hover:bg-slate-50 transition">
                    <td className="px-5 py-3.5 text-slate-500 whitespace-nowrap">{fmtDate(exp.expenseDate)}</td>
                    <td className="px-5 py-3.5 text-slate-700 font-medium">{exp.merchant}</td>
                    <td className="px-5 py-3.5 text-slate-500 max-w-xs truncate">{exp.description ?? '—'}</td>
                    <td className="px-5 py-3.5 text-slate-500">{exp.categoryName}</td>
                    <td className="px-5 py-3.5 text-right font-semibold tabular-nums text-rose-600">
                      −{fmt(exp.amount)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}

export default ExpensesPage;
