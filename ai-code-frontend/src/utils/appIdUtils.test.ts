/**
 * AppId工具函数测试
 * 验证大数值AppId的精度保持
 */

import { toAppIdString, toAppIdNumber, isLargeAppId, getAppIdForApi, isValidAppId } from './appIdUtils'

// 测试数据
const testCases = [
  // 小数值（安全整数范围内）
  { input: '123', expected: { isLarge: false, string: '123', number: 123 } },
  { input: 456, expected: { isLarge: false, string: '456', number: 456 } },
  { input: '9007199254740991', expected: { isLarge: false, string: '9007199254740991', number: 9007199254740991 } }, // Number.MAX_SAFE_INTEGER
  
  // 大数值（超过安全整数范围）
  { input: '310570939925008400', expected: { isLarge: true, string: '310570939925008400', number: 310570939925008400 } },
  { input: '9007199254740992', expected: { isLarge: true, string: '9007199254740992', number: 9007199254740992 } }, // Number.MAX_SAFE_INTEGER + 1
  { input: '1234567890123456789', expected: { isLarge: true, string: '1234567890123456789', number: 1234567890123456789 } },
  
  // 边界情况
  { input: undefined, expected: { isLarge: false, string: undefined, number: undefined } },
  { input: null, expected: { isLarge: false, string: undefined, number: undefined } },
  { input: '', expected: { isLarge: false, string: '', number: 0 } },
  { input: '0', expected: { isLarge: false, string: '0', number: 0 } },
]

console.log('=== AppId工具函数测试 ===')

testCases.forEach((testCase, index) => {
  console.log(`\n测试用例 ${index + 1}: ${testCase.input}`)
  
  // 测试 isLargeAppId
  const actualIsLarge = isLargeAppId(testCase.input)
  console.log(`  isLargeAppId: ${actualIsLarge} (期望: ${testCase.expected.isLarge})`)
  
  // 测试 toAppIdString
  const actualString = toAppIdString(testCase.input)
  console.log(`  toAppIdString: ${actualString} (期望: ${testCase.expected.string})`)
  
  // 测试 toAppIdNumber
  const actualNumber = toAppIdNumber(testCase.input)
  console.log(`  toAppIdNumber: ${actualNumber} (期望: ${testCase.expected.number})`)
  
  // 测试 getAppIdForApi
  const actualApi = getAppIdForApi(testCase.input)
  const expectedApi = testCase.expected.isLarge ? testCase.expected.string : testCase.expected.number
  console.log(`  getAppIdForApi: ${actualApi} (期望: ${expectedApi})`)
  
  // 测试 isValidAppId
  const actualValid = isValidAppId(testCase.input)
  const expectedValid = testCase.input !== undefined && testCase.input !== null && testCase.input !== '' && Number(testCase.input) > 0
  console.log(`  isValidAppId: ${actualValid} (期望: ${expectedValid})`)
  
  // 验证精度保持
  if (testCase.expected.isLarge && actualString) {
    const originalValue = String(testCase.input)
    const convertedBack = String(actualNumber)
    const precisionMaintained = originalValue === convertedBack
    console.log(`  精度保持: ${precisionMaintained ? '✅' : '❌'} (原始: ${originalValue}, 转换后: ${convertedBack})`)
  }
})

console.log('\n=== 测试完成 ===')

// 导出测试函数供其他模块使用
export function runAppIdTests() {
  console.log('运行AppId工具函数测试...')
  // 这里可以添加更详细的测试逻辑
} 