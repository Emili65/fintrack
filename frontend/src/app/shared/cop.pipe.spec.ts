import { CopPipe } from './cop.pipe';

describe('CopPipe', () => {
  const pipe = new CopPipe();

  it('formatea pesos colombianos sin decimales y con punto de miles', () => {
    const value = pipe.transform(1234567);
    expect(value).toContain('1.234.567');
    expect(value).toContain('$');
  });

  it('trata null como cero', () => {
    expect(pipe.transform(null)).toContain('0');
  });
});
