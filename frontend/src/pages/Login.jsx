import React, { useState } from 'react';
import { Form, Input, Button, message } from 'antd';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

export default function Login() {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { t } = useTranslation();

  const onFinish = async (values) => {
    setLoading(true);
    try {
      const resp = await axios.post('/api/auth/login', values);
      const token = resp.data.token;
      localStorage.setItem('jwt', token);
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      message.success(t('login.success'));
      navigate('/app');
    } catch (err) {
      message.error(t('login.fail'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <Form name="login" onFinish={onFinish} style={{ maxWidth: 360 }}>
      <Form.Item name="username" rules={[{ required: true }]} >
        <Input placeholder={t('login.username')} />
      </Form.Item>
      <Form.Item name="password" rules={[{ required: true }]} >
        <Input.Password placeholder={t('login.password')} />
      </Form.Item>
      <Form.Item>
        <Button type="primary" htmlType="submit" loading={loading}>
          {t('login.submit')}
        </Button>
      </Form.Item>
    </Form>
  );
}
