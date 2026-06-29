import { useState, useEffect } from 'react'
import {
  getCategories, createCategory, updateCategory, deleteCategory,
  getFaqs, createFaq, updateFaq, deleteFaq,
} from '../api/admin'

function InlineForm({ fields, onSubmit, onCancel, submitLabel = '저장' }) {
  const [values, setValues] = useState(() =>
    Object.fromEntries(fields.map((f) => [f.name, f.defaultValue ?? '']))
  )

  const handleChange = (name, value) => setValues((prev) => ({ ...prev, [name]: value }))

  const handleSubmit = (e) => {
    e.preventDefault()
    onSubmit(values)
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-2">
      {fields.map((f) =>
        f.type === 'textarea' ? (
          <textarea
            key={f.name}
            placeholder={f.placeholder}
            value={values[f.name]}
            onChange={(e) => handleChange(f.name, e.target.value)}
            required={f.required}
            rows={3}
            className="input-field resize-none"
          />
        ) : f.type === 'select' ? (
          <select
            key={f.name}
            value={values[f.name]}
            onChange={(e) => handleChange(f.name, e.target.value)}
            required={f.required}
            className="input-field"
          >
            <option value="">카테고리 선택</option>
            {f.options?.map((opt) => (
              <option key={opt.id} value={opt.id}>{opt.name}</option>
            ))}
          </select>
        ) : (
          <input
            key={f.name}
            type="text"
            placeholder={f.placeholder}
            value={values[f.name]}
            onChange={(e) => handleChange(f.name, e.target.value)}
            required={f.required}
            className="input-field"
          />
        )
      )}
      <div className="flex gap-2 pt-1">
        <button type="submit" className="btn-primary btn-sm">{submitLabel}</button>
        <button type="button" onClick={onCancel} className="btn-secondary btn-sm">취소</button>
      </div>
    </form>
  )
}

function CategorySection() {
  const [categories, setCategories] = useState([])
  const [showAdd, setShowAdd] = useState(false)
  const [editId, setEditId] = useState(null)

  const reload = () => getCategories().then(setCategories).catch(() => {})
  useEffect(() => { reload() }, [])

  const handleAdd = async (values) => { await createCategory(values); setShowAdd(false); reload() }
  const handleEdit = async (id, values) => { await updateCategory(id, values); setEditId(null); reload() }
  const handleDelete = async (id) => {
    if (!confirm('카테고리를 삭제하시겠습니까?')) return
    await deleteCategory(id); reload()
  }

  const ADD_FIELDS = [
    { name: 'name', placeholder: '카테고리명', required: true },
    { name: 'description', placeholder: '설명 (선택)', type: 'textarea' },
  ]

  return (
    <div className="card">
      <div className="card-header">
        <h3 className="card-title">카테고리 관리</h3>
        <button onClick={() => setShowAdd((v) => !v)} className="btn-secondary btn-sm">
          + 추가
        </button>
      </div>

      <div className="space-y-3">
        {showAdd && (
          <div className="bg-gray-50 rounded-lg p-4">
            <InlineForm fields={ADD_FIELDS} onSubmit={handleAdd}
              onCancel={() => setShowAdd(false)} submitLabel="추가" />
          </div>
        )}

        {categories.length === 0 && !showAdd ? (
          <p className="empty-state py-4 text-sm">카테고리가 없습니다</p>
        ) : (
          categories.map((cat) => (
            <div key={cat.id} className="border border-gray-100 rounded-lg px-4 py-3">
              {editId === cat.id ? (
                <InlineForm
                  fields={[
                    { name: 'name', placeholder: '카테고리명', required: true, defaultValue: cat.name },
                    { name: 'description', placeholder: '설명', type: 'textarea', defaultValue: cat.description ?? '' },
                  ]}
                  onSubmit={(v) => handleEdit(cat.id, v)}
                  onCancel={() => setEditId(null)}
                />
              ) : (
                <div className="flex items-start justify-between">
                  <div>
                    <p className="text-sm font-medium text-gray-800">{cat.name}</p>
                    {cat.description && (
                      <p className="text-xs text-gray-500 mt-0.5">{cat.description}</p>
                    )}
                  </div>
                  <div className="flex gap-2 ml-3">
                    <button onClick={() => setEditId(cat.id)} className="btn-ghost btn-sm text-primary-600">수정</button>
                    <button onClick={() => handleDelete(cat.id)} className="btn-ghost btn-sm text-error-600">삭제</button>
                  </div>
                </div>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  )
}

function FaqSection() {
  const [faqs, setFaqs] = useState([])
  const [categories, setCategories] = useState([])
  const [showAdd, setShowAdd] = useState(false)
  const [editId, setEditId] = useState(null)

  const reload = () => {
    getFaqs().then(setFaqs).catch(() => {})
    getCategories().then(setCategories).catch(() => {})
  }
  useEffect(() => { reload() }, [])

  const handleAdd = async (values) => {
    await createFaq({ ...values, categoryId: values.categoryId || null })
    setShowAdd(false); reload()
  }
  const handleEdit = async (id, values) => {
    await updateFaq(id, { ...values, categoryId: values.categoryId || null })
    setEditId(null); reload()
  }
  const handleDelete = async (id) => {
    if (!confirm('FAQ를 삭제하시겠습니까?')) return
    await deleteFaq(id); reload()
  }

  const faqFields = (defaults = {}) => [
    { name: 'question', placeholder: '질문', required: true, defaultValue: defaults.question ?? '' },
    { name: 'answer', placeholder: '답변', type: 'textarea', required: true, defaultValue: defaults.answer ?? '' },
    { name: 'categoryId', type: 'select', options: categories, defaultValue: defaults.categoryId ?? '' },
  ]

  return (
    <div className="card">
      <div className="card-header">
        <h3 className="card-title">FAQ 관리</h3>
        <button onClick={() => setShowAdd((v) => !v)} className="btn-secondary btn-sm">
          + 추가
        </button>
      </div>

      <div className="space-y-3">
        {showAdd && (
          <div className="bg-gray-50 rounded-lg p-4">
            <InlineForm fields={faqFields()} onSubmit={handleAdd}
              onCancel={() => setShowAdd(false)} submitLabel="추가" />
          </div>
        )}

        {faqs.length === 0 && !showAdd ? (
          <p className="empty-state py-4 text-sm">FAQ가 없습니다</p>
        ) : (
          faqs.map((faq) => (
            <div key={faq.id} className="border border-gray-100 rounded-lg px-4 py-3">
              {editId === faq.id ? (
                <InlineForm
                  fields={faqFields({ question: faq.question, answer: faq.answer, categoryId: faq.categoryId })}
                  onSubmit={(v) => handleEdit(faq.id, v)}
                  onCancel={() => setEditId(null)}
                />
              ) : (
                <div className="flex items-start justify-between gap-3">
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-gray-800">Q. {faq.question}</p>
                    <p className="text-sm text-gray-600 mt-1 leading-relaxed">A. {faq.answer}</p>
                    {faq.categoryName && (
                      <span className="badge-primary mt-1.5">{faq.categoryName}</span>
                    )}
                  </div>
                  <div className="flex gap-2 flex-shrink-0">
                    <button onClick={() => setEditId(faq.id)} className="btn-ghost btn-sm text-primary-600">수정</button>
                    <button onClick={() => handleDelete(faq.id)} className="btn-ghost btn-sm text-error-600">삭제</button>
                  </div>
                </div>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  )
}

export default function Admin() {
  return (
    <div className="page-content space-y-6 overflow-y-auto">
      <h2 className="page-title">관리자 페이지</h2>
      <CategorySection />
      <FaqSection />
    </div>
  )
}
