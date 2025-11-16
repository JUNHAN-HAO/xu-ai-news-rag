import React, { useState } from 'react';
import { Input, Button, List, Tag } from 'antd';
import axios from 'axios';
import { useTranslation } from 'react-i18next';

export default function SemanticSearch() {
  const [q, setQ] = useState('');
  const [results, setResults] = useState([]);
  const { t } = useTranslation();

  const search = async () => {
    const resp = await axios.post('/api/search/semantic', { query: q, top_k: 10 });
    setResults(resp.data.results || []);
  };

  return (
    <div>
      <Input.Search
        placeholder={t('search.placeholder')}
        enterButton={t('search.button')}
        value={q}
        onChange={(e) => setQ(e.target.value)}
        onSearch={search}
      />
      <List
        itemLayout="vertical"
        dataSource={results}
        renderItem={item => (
          <List.Item key={item.id}>
            <List.Item.Meta title={item.title} description={<span>{item.source} <Tag>{item.score.toFixed(3)}</Tag></span>} />
            <div dangerouslySetInnerHTML={{__html: item.snippet}} />
          </List.Item>
        )}
      />
    </div>
  );
}
